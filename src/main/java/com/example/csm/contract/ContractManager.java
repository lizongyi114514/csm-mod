package com.example.csm.contract;

import com.example.csm.Csm;
import com.example.csm.config.CsmConfig;
import com.example.csm.player.PlayerResources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * 契约账本。永久代价用 AttributeModifier 表示，数据存在玩家 NBT 里，
 * 每次登录 / 重生都按账本重新挂一遍（属性修饰符不会跟着实体复制）。
 *
 * 狐狸：3 次。第 1 次攻速 -10%，第 2 次再 -10%，第 3 次契约作废并额外 -20%。
 * 诅咒：无限次，每层最大生命 -1；每第 3 层反噬 6 点真实伤害。
 * 未来：无限次，每次最大生命 -5。
 */
public final class ContractManager {

    private static final String KEY = "contracts";
    private static final String FOX = "fox";
    private static final String CURSE = "curse";
    private static final String FUTURE = "future";
    private static final String REVOKED = "fox_revoked";

    private static final UUID FOX_ATTACK_SPEED = UUID.fromString("a1b2c3d4-0001-4a01-9a01-000000000001");
    private static final UUID CURSE_HEALTH = UUID.fromString("a1b2c3d4-0002-4a01-9a01-000000000002");
    private static final UUID FUTURE_HEALTH = UUID.fromString("a1b2c3d4-0003-4a01-9a01-000000000003");
    private static final UUID REVOKED_ATTACK_SPEED = UUID.fromString("a1b2c3d4-0004-4a01-9a01-000000000004");

    private ContractManager() {
    }

    // ---------------- 读写账本 ----------------

    private static CompoundTag ledger(Player player) {
        CompoundTag root = PlayerResources.root(player);
        if (!root.contains(KEY)) {
            root.put(KEY, new CompoundTag());
        }
        return root.getCompound(KEY);
    }

    private static int read(Player player, String key) {
        return ledger(player).getInt(key);
    }

    private static void write(Player player, String key, int value) {
        CompoundTag tag = ledger(player);
        tag.putInt(key, Math.max(0, value));
        PlayerResources.root(player).put(KEY, tag);
    }

    public static int uses(Player player, Contract contract) {
        return switch (contract) {
            case FOX -> read(player, FOX);
            case CURSE -> read(player, CURSE);
            case FUTURE -> read(player, FUTURE);
        };
    }

    public static boolean foxRevoked(Player player) {
        return ledger(player).getBoolean(REVOKED);
    }

    public static int foxRemaining(Player player) {
        return Math.max(0, Contract.FOX.maxUses() - uses(player, Contract.FOX));
    }

    public static boolean canUse(Player player, Contract contract) {
        if (contract == Contract.FOX) {
            return !foxRevoked(player) && foxRemaining(player) > 0;
        }
        return true;
    }

    // ---------------- 付账 ----------------

    public static void use(ServerPlayer player, Contract contract) {
        if (!CsmConfig.contractPenalties()) {
            return;
        }
        switch (contract) {
            case FOX -> {
                int used = uses(player, Contract.FOX) + 1;
                write(player, FOX, used);
                if (used >= Contract.FOX.maxUses()) {
                    revokeFox(player);
                } else {
                    apply(player);
                    player.displayClientMessage(Component.translatable("csm.contract.fox.paid", used,
                            Contract.FOX.maxUses() - used), true);
                }
            }
            case CURSE -> {
                int stacks = uses(player, Contract.CURSE) + 1;
                write(player, CURSE, stacks);
                apply(player);
                if (stacks % 3 == 0) {
                    player.hurt(player.damageSources().magic(), 6.0F);
                    player.displayClientMessage(Component.translatable("csm.contract.curse.backlash", stacks), false);
                }
            }
            case FUTURE -> {
                write(player, FUTURE, uses(player, Contract.FUTURE) + 1);
                apply(player);
                player.displayClientMessage(Component.translatable("csm.contract.future.paid",
                        player.getMaxHealth()), true);
            }
        }
    }

    private static void revokeFox(ServerPlayer player) {
        write(player, REVOKED, 1);
        PlayerResources.setFlag(player, "csm:contract_fox", false);
        apply(player);
        player.displayClientMessage(Component.translatable("csm.contract.fox.revoked"), false);
        Csm.LOGGER.info("[CSM] {} 的狐之契约已作废", player.getName().getString());
    }

    // ---------------- 把账本变成属性 ----------------

    /** 登录 / 重生 / 契约变化后都要调一次 */
    public static void apply(Player player) {
        boolean penalties = CsmConfig.contractPenalties();
        int fox = penalties ? uses(player, Contract.FOX) : 0;
        int curse = penalties ? uses(player, Contract.CURSE) : 0;
        int future = penalties ? uses(player, Contract.FUTURE) : 0;
        boolean revoked = penalties && foxRevoked(player);

        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(FOX_ATTACK_SPEED);
            attackSpeed.removeModifier(REVOKED_ATTACK_SPEED);
            double penalty = -0.1D * fox;
            if (penalty != 0.0D) {
                attackSpeed.addPermanentModifier(new AttributeModifier(FOX_ATTACK_SPEED, "csm_fox_cost",
                        penalty, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            if (revoked) {
                attackSpeed.addPermanentModifier(new AttributeModifier(REVOKED_ATTACK_SPEED, "csm_fox_revoked",
                        -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(CURSE_HEALTH);
            maxHealth.removeModifier(FUTURE_HEALTH);
            double penalty = -(curse + 5.0D * future);
            if (penalty != 0.0D) {
                maxHealth.addPermanentModifier(new AttributeModifier(CURSE_HEALTH, "csm_contract_health",
                        penalty, AttributeModifier.Operation.ADDITION));
            }
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }

        // 用 flag 把"契约还在不在"暴露给招式数据（requires.flag）
        PlayerResources.setFlag(player, "csm:contract_fox", !revoked);
        PlayerResources.setFlag(player, "csm:contract_curse", true);
        PlayerResources.setFlag(player, "csm:contract_future", true);
    }

    public static void reset(ServerPlayer player) {
        write(player, FOX, 0);
        write(player, CURSE, 0);
        write(player, FUTURE, 0);
        write(player, REVOKED, 0);
        apply(player);
        player.displayClientMessage(Component.translatable("csm.contract.reset"), false);
    }
}
