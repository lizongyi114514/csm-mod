package com.example.csm.damage;

import com.example.csm.Csm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 自定义伤害类型。注册表数据在 data/csm/damage_type/technique.json。
 * 这样 csm:technique 可以被 forge:tags/damage_type/combat 复用，也能被别的模组识别。
 */
public final class CsmDamage {

    public static final ResourceKey<DamageType> TECHNIQUE =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Csm.MODID, "technique"));

    private CsmDamage() {
    }

    public static DamageSource technique(LivingEntity attacker) {
        Level level = attacker.level();
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(TECHNIQUE), attacker);
    }
}
