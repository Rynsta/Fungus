package io.github.ryn.fungus.module;

import io.github.ryn.fungus.feature.BlockHighlight;
import io.github.ryn.fungus.gui.BlockHighlightScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BlockHighlightModule implements Module {
    public static final String ID = "block_highlight";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Text displayName() {
        return Text.translatable("fungus.module.blockHighlight.name");
    }

    @Override
    public boolean isEnabled() {
        return BlockHighlight.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        BlockHighlight.enabled = enabled;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public Screen createSettingsScreen(Screen parent) {
        return new BlockHighlightScreen(parent);
    }
}
