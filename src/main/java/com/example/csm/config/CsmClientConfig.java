package com.example.csm.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * 客户端配置（config/csm-client.toml）：只影响显示，不影响判定。
 * 必须只在客户端注册，否则专用服务器会崩。
 */
public final class CsmClientConfig {

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue HUD;
    private static final ForgeConfigSpec.IntValue HUD_OFFSET_X;
    private static final ForgeConfigSpec.IntValue HUD_OFFSET_Y;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("HUD").push("hud");
        HUD = builder.comment("显示技能条与能量条").define("enabled", true);
        HUD_OFFSET_X = builder.comment("整体水平偏移（负值向左）").defineInRange("offsetX", 0, -400, 400);
        HUD_OFFSET_Y = builder.comment("整体垂直偏移（负值向上）").defineInRange("offsetY", 0, -200, 200);
        builder.pop();
        SPEC = builder.build();
    }

    private CsmClientConfig() {
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC);
    }

    public static boolean hudEnabled() {
        return !SPEC.isLoaded() || HUD.get();
    }

    public static int offsetX() {
        return SPEC.isLoaded() ? HUD_OFFSET_X.get() : 0;
    }

    public static int offsetY() {
        return SPEC.isLoaded() ? HUD_OFFSET_Y.get() : 0;
    }
}
