package io.github.ryn.fungus.gui.widgets;

import io.github.ryn.fungus.gui.Theme;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class FlatSlider extends SliderWidget {
    private final Text label;
    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private final double min, max;
    private final TextRenderer tr;

    public FlatSlider(int x, int y, int w, int h, TextRenderer tr, Text label,
                      DoubleSupplier getter, DoubleConsumer setter, double min, double max) {
        super(x, y, w, h, Text.empty(), normalize(getter.getAsDouble(), min, max));
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.min = min;
        this.max = max;
        this.tr = tr;
        updateMessage();
    }

    private static double normalize(double v, double min, double max) {
        if (max <= min) return 0.0;
        double t = (v - min) / (max - min);
        return Math.max(0.0, Math.min(1.0, t));
    }

    @Override
    protected void updateMessage() {
        // unused; we render label/value ourselves
    }

    @Override
    protected void applyValue() {
        double v = min + (max - min) * this.value;
        v = Math.round(v * 100.0) / 100.0;
        setter.accept(v);
    }

    @Override
    public void renderWidget(DrawContext ctx, int mx, int my, float delta) {
        boolean hovered = isHovered();
        int x0 = getX(), y0 = getY(), x1 = getRight(), y1 = getY() + getHeight();

        ctx.fill(x0, y0, x1, y1, hovered ? Theme.ROW_HOVER : Theme.ROW_BG);

        int trackY = y1 - 3;
        ctx.fill(x0, trackY, x1, trackY + 1, Theme.DIVIDER);
        int fillX = x0 + (int) ((x1 - x0) * this.value);
        ctx.fill(x0, trackY, fillX, trackY + 1, Theme.ACCENT);

        int knobX = Math.max(x0, Math.min(x1 - 2, fillX - 1));
        ctx.fill(knobX, trackY - 2, knobX + 2, trackY + 3, Theme.ACCENT);

        ctx.drawText(tr, label, x0 + 8, y0 + 4, Theme.TEXT, false);

        double v = min + (max - min) * this.value;
        String val = String.format("%.2f", v);
        int valW = tr.getWidth(val);
        ctx.drawText(tr, val, x1 - 8 - valW, y0 + 4, Theme.ACCENT, false);
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
