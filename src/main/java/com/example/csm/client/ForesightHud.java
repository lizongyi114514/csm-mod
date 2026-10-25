package com.example.csm.client;

import com.example.csm.Csm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 预知：红色暗角 + 伤害预警，风格参考 JCraft 的 Epitaph 叠层。 */
@Mod.EventBusSubscriber(modid = Csm.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForesightHud {

    private ForesightHud() {
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "csm_foresight",
                (gui, graphics, partialTick, width, height) -> render(graphics, width, height));
    }

    private static void render(GuiGraphics graphics, int width, int height) {
        int ticks = ClientForesight.ticks();
        if (ticks <= 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        float fade = Math.min(1.0F, ticks / 20.0F);

        // 四边暗角，靠计数倒计时淡出
        for (int i = 0; i < 5; i++) {
            int alpha = (int) ((70 - i * 13) * fade);
            if (alpha <= 0) {
                continue;
            }
            int color = (alpha << 24) | 0x002B12;
            int depth = 6 + i * 6;
            int from = i * 6;
            graphics.fill(from, 0, from + 6, height, color);
            graphics.fill(width - 6 - from, 0, width - from, height, color);
            graphics.fill(0, from, width, from + 6, color);
            graphics.fill(0, height - 6 - from, width, height - from, color);
            if (depth > width / 2) {
                break;
            }
        }

        Font font = minecraft.font;
        float seconds = ticks / 20.0F;
        Component text = ClientForesight.damage() > 0.5F
                ? Component.translatable("csm.foresight.hud",
                String.format("%.1f", seconds), Math.round(ClientForesight.damage()))
                : Component.translatable("csm.foresight.safe", String.format("%.1f", seconds));
        int color = ClientForesight.damage() > 0.5F ? 0xFF6B6B : 0xFFD27A;
        int x = (width - font.width(text)) / 2;
        int y = height / 2 + 48;
        graphics.drawString(font, text, x, y, color, true);

        if (ClientForesight.threats() > 0) {
            Component threat = Component.translatable("csm.foresight.threats", ClientForesight.threats());
            graphics.drawString(font, threat, (width - font.width(threat)) / 2, y + 12, 0xFFB4A0, true);
        }
    }
}
