package io.github.ryn.fungus.mixin;

import io.github.ryn.fungus.module.PotionIconHiderModule;
import io.github.ryn.fungus.module.ScoreboardHiderModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(method = "extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"), cancellable = true)
    private void fungus$hideStatusEffectOverlay(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (PotionIconHiderModule.enabled) ci.cancel();
    }

    @Inject(method = "extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"), cancellable = true)
    private void fungus$hideScoreboardSidebar(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ScoreboardHiderModule.enabled) ci.cancel();
    }
}
