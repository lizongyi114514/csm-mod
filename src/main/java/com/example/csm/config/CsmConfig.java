package com.example.csm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 服务端/通用配置（config/csm-common.toml 自动生成）。
 * 所有 getter 都做了 isLoaded 保护：配置还没加载时返回默认值，避免启动期 NPE。
 */
public final class CsmConfig {

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.DoubleValue DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.BooleanValue PVP;
    private static final ForgeConfigSpec.DoubleValue COOLDOWN_MULTIPLIER;
    private static final ForgeConfigSpec.IntValue ENERGY_REGEN_TICKS;
    private static final ForgeConfigSpec.DoubleValue FORM_DURATION_MULTIPLIER;
    private static final ForgeConfigSpec.BooleanValue PARTICLES;
    private static final ForgeConfigSpec.DoubleValue PARTICLE_DENSITY;
    private static final ForgeConfigSpec.BooleanValue BOSS_SUMMON;
    private static final ForgeConfigSpec.BooleanValue CONTRACT_PENALTIES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("战斗数值").push("combat");
        DAMAGE_MULTIPLIER = builder
                .comment("所有招式伤害倍率。1.0 = 原值，0.5 = 减半")
                .defineInRange("damageMultiplier", 1.0D, 0.1D, 5.0D);
        PVP = builder
                .comment("允许招式伤害其他玩家。false 时技能只打怪（Boss 仍然会打玩家）")
                .define("pvp", false);
        COOLDOWN_MULTIPLIER = builder
                .comment("冷却倍率。0.5 = 冷却减半，2.0 = 翻倍")
                .defineInRange("cooldownMultiplier", 1.0D, 0.1D, 5.0D);
        builder.pop();

        builder.comment("资源与形态").push("resource");
        ENERGY_REGEN_TICKS = builder
                .comment("每多少 tick 回 1 点血（20 = 每秒 1 点）")
                .defineInRange("energyRegenTicks", 20, 5, 400);
        FORM_DURATION_MULTIPLIER = builder
                .comment("变身持续倍率。0.5 = 30 秒，2.0 = 120 秒")
                .defineInRange("formDurationMultiplier", 1.0D, 0.25D, 4.0D);
        CONTRACT_PENALTIES = builder
                .comment("契约是否真的收取代价（关掉后狐狸可以无限用）")
                .define("contractPenalties", true);
        builder.pop();

        builder.comment("表现").push("visual");
        PARTICLES = builder
                .comment("是否发送招式粒子。服务器关掉后所有玩家都看不到")
                .define("particles", true);
        PARTICLE_DENSITY = builder
                .comment("粒子密度倍率（只影响数量，不影响判定）")
                .defineInRange("particleDensity", 1.0D, 0.1D, 2.0D);
        builder.pop();

        builder.comment("其他").push("misc");
        BOSS_SUMMON = builder
                .comment("允许用 /csm summon 召唤 Boss（关掉后命令会拒绝）")
                .define("bossSummon", true);
        builder.pop();

        SPEC = builder.build();
    }

    private CsmConfig() {
    }

    public static double damageMultiplier() {
        return SPEC.isLoaded() ? DAMAGE_MULTIPLIER.get() : 1.0D;
    }

    public static boolean pvp() {
        return SPEC.isLoaded() && PVP.get();
    }

    public static double cooldownMultiplier() {
        return SPEC.isLoaded() ? COOLDOWN_MULTIPLIER.get() : 1.0D;
    }

    public static int energyRegenTicks() {
        return SPEC.isLoaded() ? ENERGY_REGEN_TICKS.get() : 20;
    }

    public static double formDurationMultiplier() {
        return SPEC.isLoaded() ? FORM_DURATION_MULTIPLIER.get() : 1.0D;
    }

    public static boolean particles() {
        return !SPEC.isLoaded() || PARTICLES.get();
    }

    public static double particleDensity() {
        return SPEC.isLoaded() ? PARTICLE_DENSITY.get() : 1.0D;
    }

    public static boolean bossSummon() {
        return !SPEC.isLoaded() || BOSS_SUMMON.get();
    }

    public static boolean contractPenalties() {
        return !SPEC.isLoaded() || CONTRACT_PENALTIES.get();
    }
}
