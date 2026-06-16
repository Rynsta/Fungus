package io.github.ryn.fungus.gui.widgets;

import io.github.ryn.fungus.gui.Theme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class FlatColorRow extends AbstractWidget {
    private final Component label;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private final Font tr;

    private static final int LABEL_W   = 78;
    private static final int SWATCH_W  = 18;
    private static final int GAP       = 2;
    private static final int CHECKER   = 0xFF2A2A2A;
    private static final int CHECKER_2 = 0xFF1F1F1F;

    private int dragChannel = -1;

    public FlatColorRow(int x, int y, int w, int h, Font tr, Component label,
                        IntSupplier getter, IntConsumer setter) {
        super(x, y, w, h, label);
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.tr = tr;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        boolean hovered = isHovered();
        int x0 = getX(), y0 = getY(), x1 = getRight(), y1 = getY() + getHeight();

        ctx.fill(x0, y0, x1, y1, hovered ? Theme.ROW_HOVER : Theme.ROW_BG);

        int color = getter.getAsInt();
        int r = (color >> 16) & 0xFF;
        int g = (color >>  8) & 0xFF;
        int b =  color        & 0xFF;
        int a = (color >> 24) & 0xFF;

        ctx.text(tr, label, x0 + 8, y0 + (getHeight() - tr.lineHeight) / 2, Theme.TEXT, false);

        int swatchX = x0 + LABEL_W;
        int swatchY = y0 + 3;
        int swatchH = getHeight() - 6;
        drawCheckerboard(ctx, swatchX, swatchY, SWATCH_W, swatchH);
        ctx.fill(swatchX, swatchY, swatchX + SWATCH_W, swatchY + swatchH, color);
        drawBorder(ctx, swatchX, swatchY, SWATCH_W, swatchH);

        int slidersX = swatchX + SWATCH_W + 6;
        int slidersW = x1 - 6 - slidersX;
        int channelW = (slidersW - GAP * 3) / 4;

        int[] vals = { r, g, b, a };
        String[] names = { "R", "G", "B", "A" };
        int[] tints = {
                0xFFE05A5A,
                0xFF5AE07A,
                0xFF5A86E0,
                0xFFCCCCCC
        };
        for (int i = 0; i < 4; i++) {
            int cx = slidersX + (channelW + GAP) * i;
            renderChannel(ctx, cx, y0, channelW, getHeight(), names[i], vals[i], tints[i], dragChannel == i);
        }
    }

    private void renderChannel(GuiGraphicsExtractor ctx, int x, int y, int w, int h,
                               String name, int value, int tint, boolean active) {
        int trackY = y + h - 4;
        ctx.fill(x, trackY, x + w, trackY + 1, Theme.DIVIDER);
        int fillX = x + (int) (w * (value / 255.0));
        ctx.fill(x, trackY, fillX, trackY + 1, tint);

        int knobX = Math.max(x, Math.min(x + w - 2, fillX - 1));
        ctx.fill(knobX, trackY - 2, knobX + 2, trackY + 3, active ? Theme.ACCENT : tint);

        int textY = y + 3;
        ctx.text(tr, name, x + 2, textY, Theme.TEXT_DIM, false);
        String v = Integer.toString(value);
        int vw = tr.width(v);
        ctx.text(tr, v, x + w - vw - 2, textY, active ? Theme.ACCENT : Theme.TEXT, false);
    }

    private static void drawCheckerboard(GuiGraphicsExtractor ctx, int x, int y, int w, int h) {
        int cell = 4;
        for (int yy = 0; yy < h; yy += cell) {
            for (int xx = 0; xx < w; xx += cell) {
                int c = ((xx / cell + yy / cell) % 2 == 0) ? CHECKER : CHECKER_2;
                int x2 = Math.min(x + xx + cell, x + w);
                int y2 = Math.min(y + yy + cell, y + h);
                ctx.fill(x + xx, y + yy, x2, y2, c);
            }
        }
    }

    private static void drawBorder(GuiGraphicsExtractor ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + 1, Theme.BORDER);
        ctx.fill(x, y + h - 1, x + w, y + h, Theme.BORDER);
        ctx.fill(x, y, x + 1, y + h, Theme.BORDER);
        ctx.fill(x + w - 1, y, x + w, y + h, Theme.BORDER);
    }

    private int channelAt(double mx) {
        int x0 = getX(), x1 = getRight();
        int slidersX = x0 + LABEL_W + SWATCH_W + 6;
        int slidersW = x1 - 6 - slidersX;
        int channelW = (slidersW - GAP * 3) / 4;
        for (int i = 0; i < 4; i++) {
            int cx = slidersX + (channelW + GAP) * i;
            if (mx >= cx && mx < cx + channelW) return i;
        }
        return -1;
    }

    private void applyChannel(int channel, double mx) {
        int x0 = getX(), x1 = getRight();
        int slidersX = x0 + LABEL_W + SWATCH_W + 6;
        int slidersW = x1 - 6 - slidersX;
        int channelW = (slidersW - GAP * 3) / 4;
        int cx = slidersX + (channelW + GAP) * channel;
        double t = (mx - cx) / (double) channelW;
        if (t < 0) t = 0; else if (t > 1) t = 1;
        int v = (int) Math.round(t * 255.0);
        int color = getter.getAsInt();
        int shift = switch (channel) {
            case 0 -> 16;
            case 1 -> 8;
            case 2 -> 0;
            default -> 24;
        };
        int mask = ~(0xFF << shift);
        int updated = (color & mask) | ((v & 0xFF) << shift);
        setter.accept(updated);
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        int ch = channelAt(click.x());
        if (ch >= 0) {
            dragChannel = ch;
            applyChannel(ch, click.x());
        }
    }

    @Override
    protected void onDrag(MouseButtonEvent click, double offsetX, double offsetY) {
        if (dragChannel >= 0) {
            applyChannel(dragChannel, click.x());
        }
    }

    @Override
    public void onRelease(MouseButtonEvent click) {
        dragChannel = -1;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        defaultButtonNarrationText(builder);
    }
}
