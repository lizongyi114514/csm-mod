package com.example.csm.skill;

import com.example.csm.config.CsmConfig;
import com.example.csm.contract.Contract;
import com.example.csm.entity.FoxDevilEntity;
import com.example.csm.entity.FutureDevilEntity;
import com.example.csm.contract.ContractManager;
import com.example.csm.form.FormManager;
import com.example.csm.network.PlayAnimPacket;
import com.example.csm.network.SkillStatePacket;
import com.example.csm.player.PlayerResources;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 施法入口：校验 -> 扣费 -> 上冷却 -> 建 runtime。
 * 同一时间只允许一个技能在跑（可用 interrupt 打断，后续再加）。
 */
public final class SkillEngine {

    private static final Map<UUID, SkillRuntime> ACTIVE = new HashMap<>();

    private SkillEngine() {
    }

    public static boolean cast(ServerPlayer player, int slot) {
        SkillDefinition definition = SkillSlots.slot(FormManager.getForm(player).school(), slot);
        return definition != null && cast(player, definition);
    }

    public static boolean cast(ServerPlayer player, SkillDefinition definition) {
        if (definition == null || !SkillManager.isLoaded()) {
            return false;
        }
        if (ACTIVE.containsKey(player.getUUID())) {
            return false;
        }
        if (!meetsRequirements(player, definition)) {
            player.displayClientMessage(Component.translatable("csm.message.requirement"), true);
            return false;
        }
        if (PlayerResources.onCooldown(player, definition.id())) {
            return false;
        }
        if (!PlayerResources.spendEnergy(player, definition.cost().energy())) {
            player.displayClientMessage(Component.translatable("csm.message.no_energy"), true);
            return false;
        }

        Contract contract = Contract.byId(definition.requires().contract());
        if (contract != null && !ContractManager.canUse(player, contract)) {
            player.displayClientMessage(Component.translatable("csm.contract.unavailable"), true);
            return false;
        }

        int cooldown = (int) Math.round(definition.cost().cooldown() * CsmConfig.cooldownMultiplier());
        PlayerResources.setCooldown(player, definition.id(), cooldown);
        ACTIVE.put(player.getUUID(), new SkillRuntime(player, definition));
        PlayAnimPacket.send(player, definition.visual().animation());
        if (contract != null) {
            ContractManager.use(player, contract);
        }
        String path = definition.id().getPath();
        if (path.equals("aki/fox_contract")) {
            FoxDevilEntity.summon(player, 200);
        } else if (path.equals("aki/future_sight")) {
            FutureDevilEntity.summon(player, 400);
        }
        SkillStatePacket.send(player);
        return true;
    }

    public static boolean cast(ServerPlayer player, ResourceLocation id) {
        return cast(player, SkillManager.byId(id));
    }

    public static void tick(ServerPlayer player) {
        SkillRuntime runtime = ACTIVE.get(player.getUUID());
        if (runtime == null) {
            return;
        }
        if (runtime.tick()) {
            ACTIVE.remove(player.getUUID());
            SkillStatePacket.send(player);
        }
    }

    public static SkillRuntime active(ServerPlayer player) {
        return ACTIVE.get(player.getUUID());
    }

    public static void clear(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
    }

    private static boolean meetsRequirements(ServerPlayer player, SkillDefinition definition) {
        SkillDefinition.Requires requires = definition.requires();
        if (player.experienceLevel < requires.level()) {
            return false;
        }
        if (!requires.flag().isEmpty() && !PlayerResources.hasFlag(player, requires.flag())) {
            return false;
        }
        if (!requires.item().isEmpty()) {
            ResourceLocation itemId = ResourceLocation.tryParse(requires.item());
            if (itemId == null) {
                return true;
            }
            boolean found = false;
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && itemId.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
