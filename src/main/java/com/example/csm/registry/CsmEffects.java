package com.example.csm.registry;

import com.example.csm.Csm;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

/**
 * 招式 JSON 里用字符串 id 引用的效果（csm:overdrive、csm:bleeding…）。
 * 名字必须与 data/csm/skills/**.json 中的 "effects[].id" 完全一致。
 */
public final class CsmEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Csm.MODID);

    /** 流血：每 2 秒 1 点魔法伤害，层数越高跳得越快 */
    public static final RegistryObject<MobEffect> BLEEDING = EFFECTS.register("bleeding", BleedingEffect::new);

    /** 全开·暴走：攻击力 +3 */
    public static final RegistryObject<MobEffect> OVERDRIVE = EFFECTS.register("overdrive",
            () -> new CsmEffect(MobEffectCategory.BENEFICIAL, 0xFF8A2B)
                    .modify(Attributes.ATTACK_DAMAGE, "9c1b7f4e-2a55-4d0e-9f1a-7e5c3b1d0a11", 3.0, AttributeModifier.Operation.ADDITION));

    /** 血盾：护甲 +4 */
    public static final RegistryObject<MobEffect> BLOOD_SHIELD = EFFECTS.register("blood_shield",
            () -> new CsmEffect(MobEffectCategory.BENEFICIAL, 0xC0392B)
                    .modify(Attributes.ARMOR, "3f2d6a18-9c44-4b7e-8a55-1d2c3e4f5a66", 4.0, AttributeModifier.Operation.ADDITION));

    /** 未来视：移速 +20% */
    public static final RegistryObject<MobEffect> FORESIGHT = EFFECTS.register("foresight",
            () -> new CsmEffect(MobEffectCategory.BENEFICIAL, 0xD9A441)
                    .modify(Attributes.MOVEMENT_SPEED, "b71e5c02-4d3a-4f88-9e11-6a2b3c4d5e77", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));

    /** 恐惧：移速 -20%，攻击 -2 */
    public static final RegistryObject<MobEffect> TERROR = EFFECTS.register("terror",
            () -> new CsmEffect(MobEffectCategory.HARMFUL, 0x2B1B1B)
                    .modify(Attributes.MOVEMENT_SPEED, "5a4c3b21-8e77-4d99-b0aa-1c2d3e4f5b88", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .modify(Attributes.ATTACK_DAMAGE, "6b5d4c32-9f88-4e00-c1bb-2d3e4f5a6c99", -2.0, AttributeModifier.Operation.ADDITION));

    /** 定身：移速 -100% */
    public static final RegistryObject<MobEffect> STUNNED = EFFECTS.register("stunned",
            () -> new CsmEffect(MobEffectCategory.HARMFUL, 0x3A3A6B)
                    .modify(Attributes.MOVEMENT_SPEED, "7c6e5d43-a099-4f11-d2cc-3e4f5a6b7d00", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));

    /** 以下只做标记，行为由各自的系统（契约 / 抹除 / 傀儡）读取 */
    public static final RegistryObject<MobEffect> BLOOD_REGENERATION = marker("blood_regeneration", 0x8E1B1B, MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> BOUND = marker("bound", 0xA31621, MobEffectCategory.HARMFUL);
    public static final RegistryObject<MobEffect> DOMINATED = marker("dominated", 0x7A1F3D, MobEffectCategory.HARMFUL);
    public static final RegistryObject<MobEffect> CURSE_MARK = marker("curse_mark", 0x4B0082, MobEffectCategory.HARMFUL);
    public static final RegistryObject<MobEffect> SACRIFICE_WARD = marker("sacrifice_ward", 0x8B0000, MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> ERASED = marker("erased", 0x101010, MobEffectCategory.HARMFUL);
    public static final RegistryObject<MobEffect> DEVOURED = marker("devoured", 0xB22222, MobEffectCategory.HARMFUL);
    /** 炸弹层数：amplifier = 层数 - 1，引爆时按层数加伤 */
    public static final RegistryObject<MobEffect> BOMB_PRIMED = marker("bomb_primed", 0xFF6B1A, MobEffectCategory.HARMFUL);

    private CsmEffects() {
    }

    private static RegistryObject<MobEffect> marker(String id, int color, MobEffectCategory category) {
        return EFFECTS.register(id, () -> new CsmEffect(category, color));
    }

    /** 带属性修饰的通用效果 */
    public static class CsmEffect extends MobEffect {
        public CsmEffect(MobEffectCategory category, int color) {
            super(category, color);
        }

        public CsmEffect modify(net.minecraft.world.entity.ai.attributes.Attribute attribute, String uuid,
                                double amount, AttributeModifier.Operation operation) {
            addAttributeModifier(attribute, uuid, amount, operation);
            return this;
        }
    }

    /** 流血：40 tick 跳一次，amplifier 每级减半间隔 */
    public static class BleedingEffect extends MobEffect {
        public BleedingEffect() {
            super(MobEffectCategory.HARMFUL, 0x7C1414);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            int interval = 40 >> Math.min(amplifier, 3);
            return interval <= 0 || duration % interval == 0;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            entity.hurt(entity.damageSources().magic(), 1.0F);
        }
    }
}
