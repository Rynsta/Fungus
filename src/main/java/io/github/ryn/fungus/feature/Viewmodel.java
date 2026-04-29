package io.github.ryn.fungus.feature;

public class Viewmodel {
    public static boolean enabled     = true;
    public static boolean noHaste     = false;
    public static boolean noEquip     = false;
    public static boolean applyToHand = false;
    public static boolean noBowSwing  = false;
    public static double  speed       = 0.0;

    public static double offsetX = 0.0, offsetY = 0.0, offsetZ = 0.0;
    public static double scaleX  = 1.0, scaleY  = 1.0, scaleZ  = 1.0;
    public static double rotX    = 0.0, rotY    = 0.0, rotZ    = 0.0;
    public static double swingX  = 1.0, swingY  = 1.0, swingZ  = 1.0;

    public static boolean isActive() {
        return enabled;
    }

    public static void resetDefaults() {
        enabled = true;
        noHaste = false;
        noEquip = false;
        applyToHand = false;
        noBowSwing = false;
        speed = 0.0;
        offsetX = offsetY = offsetZ = 0.0;
        scaleX  = scaleY  = scaleZ  = 1.0;
        rotX    = rotY    = rotZ    = 0.0;
        swingX  = swingY  = swingZ  = 1.0;
    }
}
