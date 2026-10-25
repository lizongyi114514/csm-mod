package com.example.csm.network;

import com.example.csm.contract.Contract;
import com.example.csm.contract.ContractManager;
import com.example.csm.form.FormManager;
import com.example.csm.player.PlayerResources;
import com.example.csm.skill.SkillDefinition;
import com.example.csm.skill.SkillSlots;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * 服务端 -> 客户端的战斗状态快照：能量、形态、4 个槽位的招式 id 与剩余冷却。
 * 只在施法开始/结束、变身、以及每 20 tick 的兜底同步时发送。
 */
public record SkillStatePacket(int energy, int maxEnergy, int form, String[] ids, int[] cooldowns, int[] contracts) {

    public static final int SLOTS = SkillSlots.SLOTS;

    public SkillStatePacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                readIds(buffer), readCooldowns(buffer), readContracts(buffer));
    }

    private static String[] readIds(FriendlyByteBuf buffer) {
        String[] ids = new String[SLOTS];
        for (int i = 0; i < SLOTS; i++) {
            ids[i] = buffer.readUtf();
        }
        return ids;
    }

    private static int[] readCooldowns(FriendlyByteBuf buffer) {
        int[] cooldowns = new int[SLOTS];
        for (int i = 0; i < SLOTS; i++) {
            cooldowns[i] = buffer.readVarInt();
        }
        return cooldowns;
    }

    private static int[] readContracts(FriendlyByteBuf buffer) {
        int[] contracts = new int[3];
        for (int i = 0; i < contracts.length; i++) {
            contracts[i] = buffer.readVarInt();
        }
        return contracts;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(energy);
        buffer.writeVarInt(maxEnergy);
        buffer.writeVarInt(form);
        for (int i = 0; i < SLOTS; i++) {
            buffer.writeUtf(ids[i] == null ? "" : ids[i]);
        }
        for (int i = 0; i < SLOTS; i++) {
            buffer.writeVarInt(Math.max(0, cooldowns[i]));
        }
        for (int i = 0; i < contracts.length; i++) {
            buffer.writeVarInt(Math.max(0, contracts[i]));
        }
    }

    public static void send(ServerPlayer player) {
        String[] ids = new String[SLOTS];
        int[] cooldowns = new int[SLOTS];
        for (int i = 0; i < SLOTS; i++) {
            SkillDefinition definition = SkillSlots.slot(FormManager.getForm(player).school(), i);
            ids[i] = definition == null ? "" : definition.id().toString();
            cooldowns[i] = definition == null ? 0 : PlayerResources.cooldownRemaining(player, definition.id());
        }
        int[] contracts = {
                ContractManager.foxRemaining(player),
                ContractManager.uses(player, Contract.CURSE),
                ContractManager.uses(player, Contract.FUTURE)
        };
        CsmNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SkillStatePacket(PlayerResources.energy(player), PlayerResources.maxEnergy(player),
                        FormManager.getForm(player).ordinal(), ids, cooldowns, contracts));
    }

    public static void handle(SkillStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> com.example.csm.client.ClientSkillState.accept(packet));
        context.setPacketHandled(true);
    }
}
