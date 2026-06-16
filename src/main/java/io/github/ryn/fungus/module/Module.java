package io.github.ryn.fungus.module;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public interface Module {
    String id();

    Component displayName();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    default boolean hasSettings() {
        return false;
    }

    default Screen createSettingsScreen(Screen parent) {
        return null;
    }
}
