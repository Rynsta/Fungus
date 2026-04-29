package io.github.ryn.fungus.module;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public interface Module {
    String id();

    Text displayName();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    default boolean hasSettings() {
        return false;
    }

    default Screen createSettingsScreen(Screen parent) {
        return null;
    }
}
