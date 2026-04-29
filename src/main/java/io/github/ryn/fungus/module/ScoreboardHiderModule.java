package io.github.ryn.fungus.module;

import net.minecraft.text.Text;

public class ScoreboardHiderModule implements Module {
    public static final String ID = "scoreboard_hider";

    public static boolean enabled = false;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Text displayName() {
        return Text.translatable("fungus.module.scoreboardHider.name");
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
