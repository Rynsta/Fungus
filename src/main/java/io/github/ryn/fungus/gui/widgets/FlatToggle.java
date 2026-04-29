package io.github.ryn.fungus.gui.widgets;

import io.github.ryn.fungus.gui.Theme;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FlatToggle extends ClickableWidget {
    private final Text label;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private final TextRenderer tr;

    private static final int TOGGLE_W = 20;
    private static final int TOGGLE_H = 10;

    public FlatToggle(int x, int y, int w, int h, TextRenderer tr, Text label,
                      BooleanSupplier getter, Consumer<Boolean> setter) {
        super(x, y, w, h, label);
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.tr = tr;
    }

    @Override
    public void renderWidget(DrawContext ctx, int mx, int my, float delta) {
        boolean on = getter.getAsBoolean();
        boolean hovered = isHovered();
        int x0 = getX(), y0 = getY(), x1 = getRight(), y1 = getY() + getHeight();

        ctx.fill(x0, y0, x1, y1, hovered ? Theme.ROW_HOVER : Theme.ROW_BG);
        if (on) {
            ctx.fill(x0, y0, x0 + 2, y1, Theme.ACCENT);
        }

        int textColor = on ? Theme.TEXT_BRIGHT : Theme.TEXT_OFF;
        ctx.drawText(tr, label, x0 + 8, y0 + (getHeight() - tr.fontHeight) / 2, textColor, false);

        int tx = x1 - TOGGLE_W - 6;
        int ty = y0 + (getHeight() - TOGGLE_H) / 2;
        if (on) {
            ctx.fill(tx, ty, tx + TOGGLE_W, ty + TOGGLE_H, Theme.ACCENT);
            ctx.fill(tx + TOGGLE_W - TOGGLE_H, ty + 2,
                    tx + TOGGLE_W - 2, ty + TOGGLE_H - 2, Theme.KNOB_ON);
        } else {
            ctx.fill(tx, ty, tx + TOGGLE_W, ty + TOGGLE_H, Theme.TRACK_OFF);
            ctx.fill(tx + 2, ty + 2, tx + TOGGLE_H, ty + TOGGLE_H - 2, Theme.KNOB_OFF);
        }
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        setter.accept(!getter.getAsBoolean());
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
