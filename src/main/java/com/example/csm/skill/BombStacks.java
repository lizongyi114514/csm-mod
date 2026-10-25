package com.example.csm.skill;

import com.example.csm.registry.CsmEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/** 炸弹层数：上限 5，挂在 csm:bomb_primed 的 amplifier 上（amplifier = 层数 - 1）。 */
public final class BombStacks {

    public static final int MAX = 5;
    public static final float DAMAGE_PER_STACK = 5.0F;

    private BombStacks() {
    }

    public static int get(LivingEntity entity) {
        MobEffectInstance instance = entity.getEffect(CsmEffects.BOMB_PRIMED.get());
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }

    public static void add(LivingEntity entity, int amount) {
        int total = Math.min(MAX, get(entity) + amount);
        if (total <= 0) {
            return;
        }
        entity.removeEffect(CsmEffects.BOMB_PRIMED.get());
        entity.addEffect(new MobEffectInstance(CsmEffects.BOMB_PRIMED.get(), 200, total - 1, false, true));
    }

    public static int consume(LivingEntity entity) {
        int stacks = get(entity);
        if (stacks > 0) {
            entity.removeEffect(CsmEffects.BOMB_PRIMED.get());
        }
        return stacks;
    }
}
