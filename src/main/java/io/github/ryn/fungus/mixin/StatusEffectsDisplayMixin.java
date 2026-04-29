package io.github.ryn.fungus.mixin;

import io.github.ryn.fungus.module.PotionIconHiderModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectsDisplay.class)
public class StatusEffectsDisplayMixin {

    @Inject(method = "shouldHideStatusEffectHud", at = @At("HEAD"), cancellable = true)
    private void fungus$forceHideStatusEffectHud(CallbackInfoReturnable<Boolean> cir) {
        if (PotionIconHiderModule.enabled) cir.setReturnValue(true);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;II)V", at = @At("HEAD"), cancellable = true)
    private void fungus$cancelRender(DrawContext context, int x, int y, CallbackInfo ci) {
        if (PotionIconHiderModule.enabled) ci.cancel();
    }
}
