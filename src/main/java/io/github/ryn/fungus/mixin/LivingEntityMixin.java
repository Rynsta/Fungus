package io.github.ryn.fungus.mixin;

import io.github.ryn.fungus.feature.Viewmodel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TridentItem;
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
        return (Object) this == Minecraft.getInstance().player;
    }

    @Unique
    private double fungus$durationF() {
        return Math.max(FUNGUS_MIN_DURATION, 6.0 - Viewmodel.speed);
    }

    @Inject(method = "getCurrentSwingDuration", at = @At("HEAD"), cancellable = true)
    private void fungus$swingDuration(CallbackInfoReturnable<Integer> cir) {
        if (!fungus$isCustomSwingTarget()) return;
        cir.setReturnValue(Math.max(1, (int) Math.round(fungus$durationF())));
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    private void fungus$noBowSwing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        if (!Viewmodel.isActive() || !Viewmodel.noBowSwing) return;
        if ((Object) this != Minecraft.getInstance().player) return;
        Item it = ((LivingEntity) (Object) this).getItemInHand(hand).getItem();
        if (it instanceof BowItem || it instanceof CrossbowItem || it instanceof TridentItem) {
            ci.cancel();
        }
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("TAIL"))
    private void fungus$onSwingStart(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        if (!fungus$isCustomSwingTarget()) return;
        double durF = fungus$durationF();
        if (fungus$swingElapsed < 0.0 || fungus$swingElapsed >= durF / 2.0) {
            fungus$swingElapsed = 0.0;
        }
    }

    @Inject(method = "updateSwingTime", at = @At("HEAD"))
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

    @Inject(method = "getAttackAnim", at = @At("HEAD"), cancellable = true)
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
