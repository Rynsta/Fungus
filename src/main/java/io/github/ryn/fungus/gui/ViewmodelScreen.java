package io.github.ryn.fungus.gui;

import io.github.ryn.fungus.config.ConfigManager;
import io.github.ryn.fungus.feature.Viewmodel;
import io.github.ryn.fungus.gui.widgets.FlatButton;
import io.github.ryn.fungus.gui.widgets.FlatSlider;
import io.github.ryn.fungus.gui.widgets.FlatToggle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class ViewmodelScreen extends Screen {
    private final Screen parent;

    private static final int PANEL_W      = 360;
    private static final int PANEL_H      = 300;
    private static final int TITLE_BAR_H  = 30;
    private static final int SECTION_H    = 16;
    private static final int ROW_H        = 18;
    private static final int GAP          = 3;
    private static final int LIST_PAD     = 12;
    private static final int FOOTER_H     = 28;

    private int panelX, panelY;

    public ViewmodelScreen(Screen parent) {
        super(Text.translatable("fungus.viewmodel.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = (this.width - PANEL_W) / 2;
        panelY = (this.height - PANEL_H) / 2;

        int leftX  = panelX + LIST_PAD;
        int rightX = panelX + PANEL_W / 2 + GAP / 2;
        int colW   = (PANEL_W - LIST_PAD * 2 - GAP) / 2;

        int y = panelY + TITLE_BAR_H + SECTION_H;

        addDrawableChild(new FlatToggle(leftX, y, colW, ROW_H, this.textRenderer,
                Text.translatable("fungus.viewmodel.enabled"),
                () -> Viewmodel.enabled, v -> Viewmodel.enabled = v));
        addDrawableChild(new FlatToggle(rightX, y, colW, ROW_H, this.textRenderer,
                Text.translatable("fungus.viewmodel.applyToHand"),
                () -> Viewmodel.applyToHand, v -> Viewmodel.applyToHand = v));
        y += ROW_H + GAP;

        addDrawableChild(new FlatToggle(leftX, y, colW, ROW_H, this.textRenderer,
                Text.translatable("fungus.viewmodel.noHaste"),
                () -> Viewmodel.noHaste, v -> Viewmodel.noHaste = v));
        addDrawableChild(new FlatToggle(rightX, y, colW, ROW_H, this.textRenderer,
                Text.translatable("fungus.viewmodel.noEquipAnim"),
                () -> Viewmodel.noEquip, v -> Viewmodel.noEquip = v));
        y += ROW_H + GAP;

        addDrawableChild(new FlatToggle(leftX, y, colW * 2 + GAP, ROW_H, this.textRenderer,
                Text.translatable("fungus.viewmodel.noBowSwing"),
                () -> Viewmodel.noBowSwing, v -> Viewmodel.noBowSwing = v));
        y += ROW_H + GAP * 2;

        addSlider(leftX, y, colW * 2 + GAP, "fungus.viewmodel.speed",
                () -> Viewmodel.speed, v -> Viewmodel.speed = v, -10.0, 10.0);
        y += ROW_H + GAP;

        addRow(leftX, rightX, colW, y,
                "fungus.viewmodel.offsetX", () -> Viewmodel.offsetX, v -> Viewmodel.offsetX = v, -2.0, 2.0,
                "fungus.viewmodel.offsetY", () -> Viewmodel.offsetY, v -> Viewmodel.offsetY = v, -2.0, 2.0);
        y += ROW_H + GAP;
        addRow(leftX, rightX, colW, y,
                "fungus.viewmodel.offsetZ", () -> Viewmodel.offsetZ, v -> Viewmodel.offsetZ = v, -2.0, 2.0,
                "fungus.viewmodel.scaleX", () -> Viewmodel.scaleX, v -> Viewmodel.scaleX = v, 0.1, 4.0);
        y += ROW_H + GAP;
        addRow(leftX, rightX, colW, y,
                "fungus.viewmodel.scaleY", () -> Viewmodel.scaleY, v -> Viewmodel.scaleY = v, 0.1, 4.0,
                "fungus.viewmodel.scaleZ", () -> Viewmodel.scaleZ, v -> Viewmodel.scaleZ = v, 0.1, 4.0);
        y += ROW_H + GAP;
        addRow(leftX, rightX, colW, y,
                "fungus.viewmodel.rotX", () -> Viewmodel.rotX, v -> Viewmodel.rotX = v, -180.0, 180.0,
                "fungus.viewmodel.rotY", () -> Viewmodel.rotY, v -> Viewmodel.rotY = v, -180.0, 180.0);
        y += ROW_H + GAP;
        addRow(leftX, rightX, colW, y,
                "fungus.viewmodel.rotZ", () -> Viewmodel.rotZ, v -> Viewmodel.rotZ = v, -180.0, 180.0,
                "fungus.viewmodel.swingX", () -> Viewmodel.swingX, v -> Viewmodel.swingX = v, 0.0, 2.0);
        y += ROW_H + GAP;
        addRow(leftX, rightX, colW, y,
                "fungus.viewmodel.swingY", () -> Viewmodel.swingY, v -> Viewmodel.swingY = v, 0.0, 2.0,
                "fungus.viewmodel.swingZ", () -> Viewmodel.swingZ, v -> Viewmodel.swingZ = v, 0.0, 2.0);

        int btnW = 90, btnH = 14;
        int btnY = panelY + PANEL_H - FOOTER_H + 7;
        addDrawableChild(new FlatButton(leftX, btnY, btnW, btnH,
                Text.translatable("fungus.viewmodel.reset"), this.textRenderer, false, () -> {
                    Viewmodel.resetDefaults();
                    rebuild();
                }));
        addDrawableChild(new FlatButton(panelX + PANEL_W - LIST_PAD - btnW, btnY, btnW, btnH,
                Text.translatable("fungus.back"), this.textRenderer, true, this::close));
    }

    private void addSlider(int x, int y, int w, String key,
                           DoubleSupplier g, DoubleConsumer s, double min, double max) {
        addDrawableChild(new FlatSlider(x, y, w, ROW_H, this.textRenderer,
                Text.translatable(key), g, s, min, max));
    }

    private void addRow(int leftX, int rightX, int colW, int y,
                        String keyL, DoubleSupplier getL, DoubleConsumer setL, double minL, double maxL,
                        String keyR, DoubleSupplier getR, DoubleConsumer setR, double minR, double maxR) {
        addSlider(leftX,  y, colW, keyL, getL, setL, minL, maxL);
        addSlider(rightX, y, colW, keyR, getR, setR, minR, maxR);
    }

    private void rebuild() {
        this.clearChildren();
        this.init();
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

        int titleY = y0 + (TITLE_BAR_H - this.textRenderer.fontHeight) / 2 + 2;
        ctx.drawText(this.textRenderer, "fungus", x0 + LIST_PAD, titleY, Theme.TEXT_BRIGHT, false);
        int after = x0 + LIST_PAD + this.textRenderer.getWidth("fungus");
        ctx.drawText(this.textRenderer, ".viewmodel", after, titleY, Theme.ACCENT, false);

        String hint = "[ESC] BACK";
        int hintW = this.textRenderer.getWidth(hint);
        ctx.drawText(this.textRenderer, hint, x1 - LIST_PAD - hintW, titleY, Theme.TEXT_DIM, false);

        ctx.drawText(this.textRenderer, "SETTINGS",
                x0 + LIST_PAD, y0 + TITLE_BAR_H + 5, Theme.TEXT_DIM, false);
        int sectionLineX = x0 + LIST_PAD + this.textRenderer.getWidth("SETTINGS") + 6;
        ctx.fill(sectionLineX, y0 + TITLE_BAR_H + 8, x1 - LIST_PAD,
                y0 + TITLE_BAR_H + 9, Theme.DIVIDER);

        super.render(ctx, mx, my, delta);

        ctx.fill(x0, y1 - FOOTER_H, x1, y1 - FOOTER_H + 1, Theme.DIVIDER);

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
