package io.github.ryn.fungus.module;

import io.github.ryn.fungus.feature.Viewmodel;
import io.github.ryn.fungus.gui.ViewmodelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ViewmodelModule implements Module {
    public static final String ID = "viewmodel";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Text displayName() {
        return Text.translatable("fungus.module.viewmodel.name");
    }

    @Override
    public boolean isEnabled() {
        return Viewmodel.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        Viewmodel.enabled = enabled;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public Screen createSettingsScreen(Screen parent) {
        return new ViewmodelScreen(parent);
    }
}
