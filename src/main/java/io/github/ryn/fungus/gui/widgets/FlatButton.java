package io.github.ryn.fungus.gui.widgets;

import io.github.ryn.fungus.gui.Theme;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class FlatButton extends ClickableWidget {
    private final Runnable onPress;
    private final TextRenderer tr;
    private final boolean accent;

    public FlatButton(int x, int y, int w, int h, Text label, TextRenderer tr,
                      boolean accent, Runnable onPress) {
        super(x, y, w, h, label);
        this.onPress = onPress;
        this.tr = tr;
        this.accent = accent;
    }

    @Override
    public void renderWidget(DrawContext ctx, int mx, int my, float delta) {
        boolean hovered = isHovered();
        int x0 = getX(), y0 = getY(), x1 = getRight(), y1 = getY() + getHeight();

        if (accent) {
            int bg = hovered ? Theme.ACCENT : Theme.ACCENT_DIM;
            ctx.fill(x0, y0, x1, y1, bg);
        } else {
            int bg = hovered ? Theme.ROW_HOVER_RIGHT : Theme.ROW_BG;
            ctx.fill(x0, y0, x1, y1, bg);
            int border = hovered ? Theme.ACCENT : Theme.BORDER;
            ctx.fill(x0, y0, x1, y0 + 1, border);
            ctx.fill(x0, y1 - 1, x1, y1, border);
            ctx.fill(x0, y0, x0 + 1, y1, border);
            ctx.fill(x1 - 1, y0, x1, y1, border);
        }

        Text label = getMessage();
        int textColor = accent ? 0xFF000000 : (hovered ? Theme.TEXT_BRIGHT : Theme.TEXT);
        int textX = x0 + (getWidth() - tr.getWidth(label)) / 2;
        int textY = y0 + (getHeight() - tr.fontHeight) / 2;
        ctx.drawText(tr, label, textX, textY, textColor, false);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        onPress.run();
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
