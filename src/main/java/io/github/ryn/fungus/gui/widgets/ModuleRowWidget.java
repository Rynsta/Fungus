package io.github.ryn.fungus.gui.widgets;

import io.github.ryn.fungus.gui.Theme;
import io.github.ryn.fungus.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ModuleRowWidget extends ClickableWidget {
    private final Module module;
    private final Consumer<Module> onOpenSettings;
    private final TextRenderer tr;

    private static final int SETTINGS_AREA_W = 28;
    private static final int TOGGLE_W        = 22;
    private static final int TOGGLE_H        = 11;
    private static final int ACCENT_STRIPE_W = 3;

    public ModuleRowWidget(int x, int y, int w, int h, TextRenderer tr,
                           Module module, Consumer<Module> onOpenSettings) {
        super(x, y, w, h, module.displayName());
        this.module = module;
        this.onOpenSettings = onOpenSettings;
        this.tr = tr;
    }

    @Override
    public void renderWidget(DrawContext ctx, int mx, int my, float delta) {
        boolean on = module.isEnabled();
        boolean hovered = isHovered();
        boolean overSettings = module.hasSettings() && mx >= getRight() - SETTINGS_AREA_W && hovered;

        int x0 = getX(), y0 = getY(), x1 = getRight(), y1 = getY() + getHeight();

        ctx.fill(x0, y0, x1, y1, hovered ? Theme.ROW_HOVER : Theme.ROW_BG);

        if (on) {
            ctx.fill(x0, y0, x0 + ACCENT_STRIPE_W, y1, Theme.ACCENT);
        }

        if (module.hasSettings() && overSettings) {
            ctx.fill(x1 - SETTINGS_AREA_W, y0, x1, y1, Theme.ROW_HOVER_RIGHT);
        }

        int textColor = on ? Theme.TEXT_BRIGHT : Theme.TEXT_OFF;
        ctx.drawText(tr, module.displayName(), x0 + 10,
                y0 + (getHeight() - tr.fontHeight) / 2, textColor, false);

        int toggleX = module.hasSettings()
                ? x1 - SETTINGS_AREA_W - TOGGLE_W - 10
                : x1 - TOGGLE_W - 10;
        int toggleY = y0 + (getHeight() - TOGGLE_H) / 2;
        drawToggle(ctx, toggleX, toggleY, on);

        if (module.hasSettings()) {
            int cogX = x1 - SETTINGS_AREA_W;
            drawCog(ctx, cogX + (SETTINGS_AREA_W - 9) / 2,
                    y0 + (getHeight() - 9) / 2, overSettings, hovered);
        }
    }

    private static void drawToggle(DrawContext ctx, int x, int y, boolean on) {
        if (on) {
            ctx.fill(x, y, x + TOGGLE_W, y + TOGGLE_H, Theme.ACCENT);
            ctx.fill(x + TOGGLE_W - TOGGLE_H, y + 2,
                    x + TOGGLE_W - 2, y + TOGGLE_H - 2, Theme.KNOB_ON);
        } else {
            ctx.fill(x, y, x + TOGGLE_W, y + TOGGLE_H, Theme.TRACK_OFF);
            ctx.fill(x, y, x + TOGGLE_W, y + 1, Theme.TRACK_OFF_BORDER);
            ctx.fill(x, y + TOGGLE_H - 1, x + TOGGLE_W, y + TOGGLE_H, Theme.TRACK_OFF_BORDER);
            ctx.fill(x + 2, y + 2, x + TOGGLE_H, y + TOGGLE_H - 2, Theme.KNOB_OFF);
        }
    }

    private static void drawCog(DrawContext ctx, int x, int y, boolean overSettings, boolean hovered) {
        int color = overSettings ? Theme.ACCENT : Theme.TEXT_DIM;
        ctx.fill(x + 3, y, x + 6, y + 2, color);
        ctx.fill(x + 3, y + 7, x + 6, y + 9, color);
        ctx.fill(x, y + 3, x + 2, y + 6, color);
        ctx.fill(x + 7, y + 3, x + 9, y + 6, color);
        ctx.fill(x + 2, y + 2, x + 7, y + 7, color);
        int holeColor = overSettings ? Theme.ROW_HOVER_RIGHT : (hovered ? Theme.ROW_HOVER : Theme.ROW_BG);
        ctx.fill(x + 4, y + 4, x + 5, y + 5, holeColor);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        if (module.hasSettings() && click.x() >= getRight() - SETTINGS_AREA_W) {
            onOpenSettings.accept(module);
        } else {
            module.setEnabled(!module.isEnabled());
        }
    }

    @Override
    public Text getMessage() {
        return module.displayName();
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
