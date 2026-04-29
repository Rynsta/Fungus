package io.github.ryn.fungus.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Modules {
    private static final List<Module> REGISTRY = new ArrayList<>();

    public static final ViewmodelModule VIEWMODEL = new ViewmodelModule();
    public static final PotionIconHiderModule POTION_ICON_HIDER = new PotionIconHiderModule();
    public static final ScoreboardHiderModule SCOREBOARD_HIDER = new ScoreboardHiderModule();

    static {
        REGISTRY.add(VIEWMODEL);
        REGISTRY.add(POTION_ICON_HIDER);
        REGISTRY.add(SCOREBOARD_HIDER);
    }

    private Modules() {}

    public static List<Module> all() {
        return Collections.unmodifiableList(REGISTRY);
    }
}
