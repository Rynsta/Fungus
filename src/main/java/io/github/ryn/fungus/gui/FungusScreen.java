package io.github.ryn.fungus.gui;

import io.github.ryn.fungus.config.ConfigManager;
import io.github.ryn.fungus.gui.widgets.FlatButton;
import io.github.ryn.fungus.gui.widgets.ModuleRowWidget;
import io.github.ryn.fungus.module.Module;
import io.github.ryn.fungus.module.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class FungusScreen extends Screen {
    private final Screen parent;

    private static final int PANEL_W      = 320;
    private static final int PANEL_H      = 220;
    private static final int TITLE_BAR_H  = 30;
    private static final int SECTION_H    = 16;
    private static final int ROW_H        = 26;
    private static final int ROW_GAP      = 2;
    private static final int LIST_PAD     = 12;
    private static final int FOOTER_H     = 22;

    private int panelX, panelY;

    public FungusScreen(Screen parent) {
        super(Text.translatable("fungus.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = (this.width - PANEL_W) / 2;
        panelY = (this.height - PANEL_H) / 2;

        int rowX = panelX + LIST_PAD;
        int rowW = PANEL_W - LIST_PAD * 2;
        int rowY = panelY + TITLE_BAR_H + SECTION_H;

        for (Module module : Modules.all()) {
            addDrawableChild(new ModuleRowWidget(
                    rowX, rowY, rowW, ROW_H, this.textRenderer, module, this::openSettings));
            rowY += ROW_H + ROW_GAP;
        }

        int closeW = 80;
        int closeX = panelX + PANEL_W - LIST_PAD - closeW;
        int closeY = panelY + PANEL_H - FOOTER_H + 4;
        addDrawableChild(new FlatButton(closeX, closeY, closeW, 14,
                Text.translatable("fungus.done"), this.textRenderer, true, this::close));
    }

    private void openSettings(Module module) {
        Screen settings = module.createSettingsScreen(this);
        if (settings != null && this.client != null) {
            this.client.setScreen(settings);
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, this.width, this.height, Theme.SCRIM);

        int x0 = panelX, y0 = panelY;
        int x1 = panelX + PANEL_W, y1 = panelY + PANEL_H;

        ctx.fill(x0, y0, x1, y1, Theme.PANEL_BG);
        ctx.fill(x0, y0, x1, y0 + TITLE_BAR_H, Theme.TITLE_BAR_BG);
        ctx.fill(x0, y0, x1, y0 + 2, Theme.ACCENT);
        ctx.fill(x0, y0 + TITLE_BAR_H, x1, y0 + TITLE_BAR_H + 1, Theme.DIVIDER);

        int titleX = x0 + LIST_PAD;
        int titleY = y0 + (TITLE_BAR_H - this.textRenderer.fontHeight) / 2 + 2;
        ctx.drawText(this.textRenderer, "fungus", titleX, titleY, Theme.TEXT_BRIGHT, false);
        int afterTitle = titleX + this.textRenderer.getWidth("fungus");
        ctx.drawText(this.textRenderer, ".client", afterTitle, titleY, Theme.ACCENT, false);

        String version = "v0.1.0";
        int verW = this.textRenderer.getWidth(version);
        ctx.drawText(this.textRenderer, version, x1 - LIST_PAD - verW, titleY, Theme.TEXT_DIM, false);

        ctx.drawText(this.textRenderer, "MODULES",
                x0 + LIST_PAD, y0 + TITLE_BAR_H + 5, Theme.TEXT_DIM, false);
        int sectionLineX = x0 + LIST_PAD + this.textRenderer.getWidth("MODULES") + 6;
        ctx.fill(sectionLineX, y0 + TITLE_BAR_H + 8, x1 - LIST_PAD,
                y0 + TITLE_BAR_H + 9, Theme.DIVIDER);

        super.render(ctx, mx, my, delta);

        ctx.fill(x0, y1 - FOOTER_H, x1, y1 - FOOTER_H + 1, Theme.DIVIDER);
        String hint = "[ESC] CLOSE";
        ctx.drawText(this.textRenderer, hint,
                x0 + LIST_PAD, y1 - FOOTER_H + 8, Theme.TEXT_DIM, false);

        drawBorder(ctx, x0, y0, PANEL_W, PANEL_H, Theme.BORDER);
    }

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void removed() {
        ConfigManager.save();
    }
}
