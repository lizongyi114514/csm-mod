package com.example.csm.armor;

import com.example.csm.registry.CsmItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

import java.util.UUID;

/** 电锯护甲全套：+2 攻击、+8% 移速，脱一件就失效。 */
public final class ChainsawSetBonus {

    private static final UUID ATTACK = UUID.fromString("b3f1c2d4-1111-4a2b-8c3d-0000000000a1");
    private static final UUID SPEED = UUID.fromString("b3f1c2d4-2222-4a2b-8c3d-0000000000a2");

    private ChainsawSetBonus() {
    }

    public static void tick(ServerPlayer player) {
        boolean full = isChainsaw(player.getItemBySlot(EquipmentSlot.HEAD).getItem())
                && isChainsaw(player.getItemBySlot(EquipmentSlot.CHEST).getItem())
                && isChainsaw(player.getItemBySlot(EquipmentSlot.LEGS).getItem())
                && isChainsaw(player.getItemBySlot(EquipmentSlot.FEET).getItem());

        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attack == null || speed == null) {
            return;
        }
        if (full) {
            if (attack.getModifier(ATTACK) == null) {
                attack.addTransientModifier(new AttributeModifier(ATTACK, "csm_set_attack", 2.0D,
                        AttributeModifier.Operation.ADDITION));
            }
            if (speed.getModifier(SPEED) == null) {
                speed.addTransientModifier(new AttributeModifier(SPEED, "csm_set_speed", 0.08D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        } else {
            if (attack.getModifier(ATTACK) != null) {
                attack.removeModifier(ATTACK);
            }
            if (speed.getModifier(SPEED) != null) {
                speed.removeModifier(SPEED);
            }
        }
    }

    private static boolean isChainsaw(Item item) {
        return item == CsmItems.CHAINSAW_HELMET.get() || item == CsmItems.CHAINSAW_CHESTPLATE.get()
                || item == CsmItems.CHAINSAW_LEGGINGS.get() || item == CsmItems.CHAINSAW_BOOTS.get();
    }
}
