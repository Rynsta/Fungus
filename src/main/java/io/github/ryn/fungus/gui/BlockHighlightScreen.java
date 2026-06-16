package io.github.ryn.fungus.gui;

import io.github.ryn.fungus.config.ConfigManager;
import io.github.ryn.fungus.feature.BlockHighlight;
import io.github.ryn.fungus.gui.widgets.FlatButton;
import io.github.ryn.fungus.gui.widgets.FlatColorRow;
import io.github.ryn.fungus.gui.widgets.FlatToggle;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BlockHighlightScreen extends Screen {
    private final Screen parent;

    private static final int PANEL_W      = 360;
    private static final int PANEL_H      = 220;
    private static final int TITLE_BAR_H  = 30;
    private static final int SECTION_H    = 16;
    private static final int ROW_H        = 18;
    private static final int GAP          = 3;
    private static final int LIST_PAD     = 12;
    private static final int FOOTER_H     = 28;

    private int panelX, panelY;

    public BlockHighlightScreen(Screen parent) {
        super(Component.translatable("fungus.blockHighlight.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = (this.width - PANEL_W) / 2;
        panelY = (this.height - PANEL_H) / 2;

        int leftX  = panelX + LIST_PAD;
        int rightX = panelX + PANEL_W / 2 + GAP / 2;
        int colW   = (PANEL_W - LIST_PAD * 2 - GAP) / 2;
        int rowW   = PANEL_W - LIST_PAD * 2;

        int y = panelY + TITLE_BAR_H + SECTION_H;

        addRenderableWidget(new FlatToggle(leftX, y, colW, ROW_H, this.font,
                Component.translatable("fungus.blockHighlight.enabled"),
                () -> BlockHighlight.enabled, v -> BlockHighlight.enabled = v));
        addRenderableWidget(new FlatToggle(rightX, y, colW, ROW_H, this.font,
                Component.translatable("fungus.blockHighlight.wrap"),
                () -> BlockHighlight.wrapEnabled, v -> BlockHighlight.wrapEnabled = v));
        y += ROW_H + GAP;

        addRenderableWidget(new FlatToggle(leftX, y, colW, ROW_H, this.font,
                Component.translatable("fungus.blockHighlight.fill"),
                () -> BlockHighlight.fillEnabled, v -> BlockHighlight.fillEnabled = v));
        addRenderableWidget(new FlatToggle(rightX, y, colW, ROW_H, this.font,
                Component.translatable("fungus.blockHighlight.outline"),
                () -> BlockHighlight.outlineEnabled, v -> BlockHighlight.outlineEnabled = v));
        y += ROW_H + GAP * 2;

        addRenderableWidget(new FlatColorRow(leftX, y, rowW, ROW_H, this.font,
                Component.translatable("fungus.blockHighlight.fillColor"),
                () -> BlockHighlight.fillColor, v -> BlockHighlight.fillColor = v));
        y += ROW_H + GAP;

        addRenderableWidget(new FlatColorRow(leftX, y, rowW, ROW_H, this.font,
                Component.translatable("fungus.blockHighlight.outlineColor"),
                () -> BlockHighlight.outlineColor, v -> BlockHighlight.outlineColor = v));

        int btnW = 90, btnH = 14;
        int btnY = panelY + PANEL_H - FOOTER_H + 7;
        addRenderableWidget(new FlatButton(leftX, btnY, btnW, btnH,
                Component.translatable("fungus.blockHighlight.reset"), this.font, false, () -> {
            BlockHighlight.resetDefaults();
            rebuild();
        }));
        addRenderableWidget(new FlatButton(panelX + PANEL_W - LIST_PAD - btnW, btnY, btnW, btnH,
                Component.translatable("fungus.back"), this.font, true, this::onClose));
    }

    private void rebuild() {
        this.clearWidgets();
        this.init();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, this.width, this.height, Theme.SCRIM);

        int x0 = panelX, y0 = panelY;
        int x1 = panelX + PANEL_W, y1 = panelY + PANEL_H;

        ctx.fill(x0, y0, x1, y1, Theme.PANEL_BG);
        ctx.fill(x0, y0, x1, y0 + TITLE_BAR_H, Theme.TITLE_BAR_BG);
        ctx.fill(x0, y0, x1, y0 + 2, Theme.ACCENT);
        ctx.fill(x0, y0 + TITLE_BAR_H, x1, y0 + TITLE_BAR_H + 1, Theme.DIVIDER);

        int titleY = y0 + (TITLE_BAR_H - this.font.lineHeight) / 2 + 2;
        ctx.text(this.font, "fungus", x0 + LIST_PAD, titleY, Theme.TEXT_BRIGHT, false);
        int after = x0 + LIST_PAD + this.font.width("fungus");
        ctx.text(this.font, ".blockhighlight", after, titleY, Theme.ACCENT, false);

        String hint = "[ESC] BACK";
        int hintW = this.font.width(hint);
        ctx.text(this.font, hint, x1 - LIST_PAD - hintW, titleY, Theme.TEXT_DIM, false);

        ctx.text(this.font, "SETTINGS",
                x0 + LIST_PAD, y0 + TITLE_BAR_H + 5, Theme.TEXT_DIM, false);
        int sectionLineX = x0 + LIST_PAD + this.font.width("SETTINGS") + 6;
        ctx.fill(sectionLineX, y0 + TITLE_BAR_H + 8, x1 - LIST_PAD,
                y0 + TITLE_BAR_H + 9, Theme.DIVIDER);

        super.extractRenderState(ctx, mx, my, delta);

        ctx.fill(x0, y1 - FOOTER_H, x1, y1 - FOOTER_H + 1, Theme.DIVIDER);

        drawBorder(ctx, x0, y0, PANEL_W, PANEL_H, Theme.BORDER);
    }

    private static void drawBorder(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    @Override
    public void removed() {
        ConfigManager.save();
    }
}
