package io.github.ryn.fungus.mixin;

import io.github.ryn.fungus.feature.Viewmodel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique private static final double FUNGUS_MIN_DURATION = 0.1;

    @Unique private double fungus$swingElapsed = -1.0;

    @Unique
    private boolean fungus$isCustomSwingTarget() {
        if (!Viewmodel.isActive()) return false;
        if (!Viewmodel.noHaste && Viewmodel.speed == 0.0) return false;
        return (Object) this == MinecraftClient.getInstance().player;
    }

    @Unique
    private double fungus$durationF() {
        return Math.max(FUNGUS_MIN_DURATION, 6.0 - Viewmodel.speed);
    }

    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void fungus$swingDuration(CallbackInfoReturnable<Integer> cir) {
        if (!fungus$isCustomSwingTarget()) return;
        cir.setReturnValue(Math.max(1, (int) Math.round(fungus$durationF())));
    }

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("HEAD"), cancellable = true)
    private void fungus$noBowSwing(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
        if (!Viewmodel.isActive() || !Viewmodel.noBowSwing) return;
        if ((Object) this != MinecraftClient.getInstance().player) return;
        Item it = ((LivingEntity) (Object) this).getStackInHand(hand).getItem();
        if (it instanceof BowItem || it instanceof CrossbowItem || it instanceof TridentItem) {
            ci.cancel();
        }
    }

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("TAIL"))
    private void fungus$onSwingStart(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
        if (!fungus$isCustomSwingTarget()) return;
        double durF = fungus$durationF();
        if (fungus$swingElapsed < 0.0 || fungus$swingElapsed >= durF / 2.0) {
            fungus$swingElapsed = 0.0;
        }
    }

    @Inject(method = "tickHandSwing", at = @At("HEAD"))
    private void fungus$tickHandSwing(CallbackInfo ci) {
        if (!fungus$isCustomSwingTarget()) {
            fungus$swingElapsed = -1.0;
            return;
        }
        if (fungus$swingElapsed >= 0.0) {
            fungus$swingElapsed += 1.0;
            if (fungus$swingElapsed >= fungus$durationF()) {
                fungus$swingElapsed = -1.0;
            }
        }
    }

    @Inject(method = "getHandSwingProgress", at = @At("HEAD"), cancellable = true)
    private void fungus$getHandSwingProgress(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (!fungus$isCustomSwingTarget()) return;
        if (fungus$swingElapsed < 0.0) {
            cir.setReturnValue(0.0f);
            return;
        }
        double durF = fungus$durationF();
        double t = fungus$swingElapsed + tickDelta;
        double progress = t / durF;
        if (progress >= 1.0 || progress < 0.0) {
            cir.setReturnValue(0.0f);
            return;
        }
        cir.setReturnValue((float) progress);
    }
}
