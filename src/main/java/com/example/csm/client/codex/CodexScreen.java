package com.example.csm.client.codex;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * 极简图鉴：左侧分区，右侧滚动正文。
 * 数据来自 CodexData（JSON），这里只负责画。
 */
public class CodexScreen extends Screen {

    private static final int PANEL_W = 380;
    private static final int PANEL_H = 220;
    private static final int LEFT_W = 86;

    private List<CodexData.Section> sections = List.of();
    private int selected;
    private int scroll;

    public CodexScreen() {
        super(Component.translatable("csm.codex.title"));
    }

    @Override
    protected void init() {
        this.sections = CodexData.sections();
        this.selected = Math.min(this.selected, Math.max(0, this.sections.size() - 1));
        this.scroll = 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int x = (this.width - PANEL_W) / 2;
        int y = (this.height - PANEL_H) / 2;
        graphics.fill(x, y, x + PANEL_W, y + PANEL_H, 0xF0120E10);
        graphics.renderOutline(x, y, PANEL_W, PANEL_H, 0xFF7C1414);

        // 标题
        graphics.drawString(this.font, this.title, x + 10, y + 8, 0xFFB257, false);
        graphics.fill(x + 8, y + 20, x + PANEL_W - 8, y + 21, 0x607C1414);

        // 左侧分区
        int listY = y + 28;
        for (int i = 0; i < this.sections.size(); i++) {
            int itemY = listY + i * 14;
            boolean active = i == this.selected;
            if (active) {
                graphics.fill(x + 6, itemY - 2, x + 6 + LEFT_W, itemY + 10, 0x507C1414);
            }
            graphics.drawString(this.font, this.sections.get(i).title(), x + 10, itemY,
                    active ? 0xFFB257 : 0xB0B0B0, false);
        }

        if (this.sections.isEmpty()) {
            graphics.drawString(this.font, "no codex data", x + LEFT_W + 20, y + 40, 0xCCCCCC, false);
            return;
        }

        // 右侧正文
        int contentX = x + LEFT_W + 12;
        int contentTop = y + 28;
        int contentBottom = y + PANEL_H - 12;
        int contentWidth = PANEL_W - LEFT_W - 24;

        graphics.enableScissor(contentX, contentTop, x + PANEL_W - 8, contentBottom);
        int cursorY = contentTop - this.scroll;

        for (CodexData.Entry entry : this.sections.get(this.selected).entries()) {
            graphics.drawString(this.font, entry.title(), contentX, cursorY, 0xE8E4D8, false);
            cursorY += 11;
            for (String line : entry.body()) {
                for (FormattedCharSequence wrapped : this.font.split(Component.literal(line), contentWidth)) {
                    graphics.drawString(this.font, wrapped, contentX, cursorY, 0x9AA0A6, false);
                    cursorY += 10;
                }
            }
            cursorY += 7;
        }
        graphics.disableScissor();

        graphics.drawString(this.font, Component.translatable("csm.codex.hint"), x + 10, y + PANEL_H - 12,
                0x707070, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - PANEL_W) / 2;
        int y = (this.height - PANEL_H) / 2;
        int listY = y + 28;
        for (int i = 0; i < this.sections.size(); i++) {
            int itemY = listY + i * 14;
            if (mouseX >= x + 6 && mouseX <= x + 6 + LEFT_W && mouseY >= itemY - 2 && mouseY <= itemY + 10) {
                this.selected = i;
                this.scroll = 0;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.scroll = Math.max(0, this.scroll - (int) (delta * 12));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
