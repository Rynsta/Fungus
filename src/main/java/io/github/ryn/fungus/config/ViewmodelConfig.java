package io.github.ryn.fungus.config;

import io.github.ryn.fungus.feature.Viewmodel;
import io.github.ryn.fungus.module.PotionIconHiderModule;
import io.github.ryn.fungus.module.ScoreboardHiderModule;

public class ViewmodelConfig {
    public boolean enabled     = true;
    public boolean noHaste     = false;
    public boolean noEquip     = false;
    public boolean applyToHand = false;
    public boolean noBowSwing  = false;
    public double  speed       = 0.0;

    public double offsetX = 0.0, offsetY = 0.0, offsetZ = 0.0;
    public double scaleX  = 1.0, scaleY  = 1.0, scaleZ  = 1.0;
    public double rotX    = 0.0, rotY    = 0.0, rotZ    = 0.0;
    public double swingX  = 1.0, swingY  = 1.0, swingZ  = 1.0;

    public boolean potionIconHiderEnabled = false;
    public boolean scoreboardHiderEnabled = false;

    public static ViewmodelConfig fromLive() {
        ViewmodelConfig c = new ViewmodelConfig();
        c.enabled     = Viewmodel.enabled;
        c.noHaste     = Viewmodel.noHaste;
        c.noEquip     = Viewmodel.noEquip;
        c.applyToHand = Viewmodel.applyToHand;
        c.noBowSwing  = Viewmodel.noBowSwing;
        c.speed       = Viewmodel.speed;
        c.offsetX = Viewmodel.offsetX; c.offsetY = Viewmodel.offsetY; c.offsetZ = Viewmodel.offsetZ;
        c.scaleX  = Viewmodel.scaleX;  c.scaleY  = Viewmodel.scaleY;  c.scaleZ  = Viewmodel.scaleZ;
        c.rotX    = Viewmodel.rotX;    c.rotY    = Viewmodel.rotY;    c.rotZ    = Viewmodel.rotZ;
        c.swingX  = Viewmodel.swingX;  c.swingY  = Viewmodel.swingY;  c.swingZ  = Viewmodel.swingZ;
        c.potionIconHiderEnabled = PotionIconHiderModule.enabled;
        c.scoreboardHiderEnabled = ScoreboardHiderModule.enabled;
        return c;
    }

    public void applyToLive() {
        Viewmodel.enabled     = enabled;
        Viewmodel.noHaste     = noHaste;
        Viewmodel.noEquip     = noEquip;
        Viewmodel.applyToHand = applyToHand;
        Viewmodel.noBowSwing  = noBowSwing;
        Viewmodel.speed       = speed;
        Viewmodel.offsetX = offsetX; Viewmodel.offsetY = offsetY; Viewmodel.offsetZ = offsetZ;
        Viewmodel.scaleX  = scaleX;  Viewmodel.scaleY  = scaleY;  Viewmodel.scaleZ  = scaleZ;
        Viewmodel.rotX    = rotX;    Viewmodel.rotY    = rotY;    Viewmodel.rotZ    = rotZ;
        Viewmodel.swingX  = swingX;  Viewmodel.swingY  = swingY;  Viewmodel.swingZ  = swingZ;
        PotionIconHiderModule.enabled = potionIconHiderEnabled;
        ScoreboardHiderModule.enabled = scoreboardHiderEnabled;
    }
}
