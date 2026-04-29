package io.github.ryn.fungus.module;

import net.minecraft.text.Text;

public class PotionIconHiderModule implements Module {
    public static final String ID = "potion_icon_hider";

    public static boolean enabled = false;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Text displayName() {
        return Text.translatable("fungus.module.potionIconHider.name");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean v) {
        enabled = v;
    }
}
