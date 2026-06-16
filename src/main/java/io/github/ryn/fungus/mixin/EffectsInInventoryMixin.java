package io.github.ryn.fungus.mixin;

import io.github.ryn.fungus.module.PotionIconHiderModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EffectsInInventory.class)
public class EffectsInInventoryMixin {

    // canSeeEffects() gates whether the inventory screen shows the potion-effect column; force it off to hide.
    @Inject(method = "canSeeEffects", at = @At("HEAD"), cancellable = true)
    private void fungus$forceHideStatusEffectHud(CallbackInfoReturnable<Boolean> cir) {
        if (PotionIconHiderModule.enabled) cir.setReturnValue(false);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
            at = @At("HEAD"), cancellable = true)
    private void fungus$cancelRender(GuiGraphicsExtractor context, int x, int y, CallbackInfo ci) {
        if (PotionIconHiderModule.enabled) ci.cancel();
    }
}
