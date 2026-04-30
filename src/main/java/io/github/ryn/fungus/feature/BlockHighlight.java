package io.github.ryn.fungus.feature;

public class BlockHighlight {
    public static boolean enabled       = false;
    public static boolean fillEnabled   = true;
    public static boolean outlineEnabled= true;
    public static boolean wrapEnabled   = true;

    public static int fillColor    = 0x40FFFFFF;
    public static int outlineColor = 0xC0FFFFFF;

    public static boolean isActive() {
        return enabled;
    }

    public static void resetDefaults() {
        enabled        = false;
        fillEnabled    = true;
        outlineEnabled = true;
        wrapEnabled    = true;
        fillColor      = 0x40FFFFFF;
        outlineColor   = 0xC0FFFFFF;
    }
}
