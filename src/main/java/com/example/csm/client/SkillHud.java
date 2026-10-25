package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.config.CsmClientConfig;
import com.example.csm.form.Form;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 技能条：4 个槽位 + 能量条 + 形态名。
 * 如果用别的 Forge 版本编译报错，改 registerAbove 的第一个参数即可（HOTBAR.id()）。
 */
@Mod.EventBusSubscriber(modid = Csm.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SkillHud {

    private static final int SLOT_SIZE = 20;
    private static final int SLOT_GAP = 2;
    private static final int BAR_WIDTH = 182;

    private SkillHud() {
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "csm_skillbar",
                (gui, graphics, partialTick, width, height) -> render(graphics, width, height));
    }

    private static void render(GuiGraphics graphics, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !CsmClientConfig.hudEnabled()) {
            return;
        }
        Font font = minecraft.font;

        // ---- 能量条 ----
        int barX = width / 2 - BAR_WIDTH / 2 + CsmClientConfig.offsetX();
        int barY = height - 39 + CsmClientConfig.offsetY();
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + 5, 0x90000000);
        int filled = (int) (BAR_WIDTH * (ClientSkillState.energy() / (float) Math.max(1, ClientSkillState.maxEnergy())));
        graphics.fill(barX, barY, barX + filled, barY + 5, 0xFF8E1B1B);
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + 1, 0x40FFFFFF);

        String energyText = ClientSkillState.energy() + " / " + ClientSkillState.maxEnergy();
        graphics.drawString(font, energyText, barX + BAR_WIDTH + 4, barY - 1, 0xFFFFFF, true);

        // ---- 形态名 ----
        Form form = Form.values()[Math.floorMod(ClientSkillState.form(), Form.values().length)];
        Component formName = Component.translatable("csm.form." + form.id());
        graphics.drawString(font, formName, barX, barY - 12, 0xFFB257, true);

        // ---- 4 个技能槽 ----
        int totalWidth = 4 * SLOT_SIZE + 3 * SLOT_GAP;
        int startX = width / 2 - totalWidth / 2 + CsmClientConfig.offsetX();
        int slotY = height - 66 + CsmClientConfig.offsetY();

        for (int i = 0; i < 4; i++) {
            int x = startX + i * (SLOT_SIZE + SLOT_GAP);
            graphics.fill(x, slotY, x + SLOT_SIZE, slotY + SLOT_SIZE, 0xA0101010);
            graphics.renderOutline(x, slotY, SLOT_SIZE, SLOT_SIZE, 0x60FFFFFF);

            String id = ClientSkillState.id(i);
            int cooldown = ClientSkillState.cooldown(i);

            if (cooldown > 0) {
                graphics.fill(x, slotY, x + SLOT_SIZE, slotY + SLOT_SIZE, 0xB0000000);
                String seconds = Integer.toString((cooldown + 19) / 20);
                graphics.drawString(font, seconds, x + SLOT_SIZE - 2 - font.width(seconds), slotY + 6, 0xFFFFFF, true);
            } else if (!id.isEmpty()) {
                String label = shortLabel(id);
                graphics.drawString(font, label, x + 2, slotY + 6, 0xE8E4D8, true);
            }

            graphics.drawString(font, Integer.toString(i + 1), x + 1, slotY + 1, 0x808080, false);
        }

        // ---- 契约账本 ----
        int contractX = startX + totalWidth + 6;
        drawContract(graphics, font, contractX, slotY, "csm.contract.fox", ClientSkillState.contract(0) + "/3");
        drawContract(graphics, font, contractX, slotY + 10, "csm.contract.curse", Integer.toString(ClientSkillState.contract(1)));
        drawContract(graphics, font, contractX, slotY + 20, "csm.contract.future", Integer.toString(ClientSkillState.contract(2)));
    }

    private static void drawContract(GuiGraphics graphics, Font font, int x, int y, String nameKey, String value) {
        graphics.drawString(font, Component.translatable(nameKey), x, y, 0x8B0000, true);
        graphics.drawString(font, value, x + font.width(Component.translatable(nameKey)) + 3, y, 0xD0D0D0, true);
    }

    private static String shortLabel(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return "?";
        }
        // 翻译键由 id 推导：csm:denji/chainsaw_rush -> skill.csm.denji.chainsaw_rush
        String key = "skill." + location.getNamespace() + "." + location.getPath().replace('/', '.');
        String translated = Component.translatable(key).getString();
        if (!translated.equals(key)) {
            return translated.length() > 5 ? translated.substring(0, 5) : translated;
        }
        String path = location.getPath();
        int slash = path.lastIndexOf('/');
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        return name.length() > 4 ? name.substring(0, 4) : name;
    }
}
