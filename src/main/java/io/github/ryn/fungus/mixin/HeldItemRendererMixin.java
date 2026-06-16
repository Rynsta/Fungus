package io.github.ryn.fungus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.ryn.fungus.feature.Viewmodel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {
    @Shadow private float mainHandHeight;
    @Shadow private float offHandHeight;
    @Shadow private float oMainHandHeight;
    @Shadow private float oOffHandHeight;

    @Inject(
            method = "renderArmWithItem",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER)
    )
    private void fungus$beforeRenderItem(AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand,
                                         float swingProgress, ItemStack item, float equipProgress, PoseStack matrices,
                                         SubmitNodeCollector queue, int light, CallbackInfo ci) {
        if (!Viewmodel.isActive()) return;
        if (!Viewmodel.applyToHand && item.isEmpty()) return;

        double ox = Viewmodel.offsetX;
        double oy = Viewmodel.offsetY;
        double oz = Viewmodel.offsetZ;
        if (ox == 0.0 && oy == 0.0 && oz == 0.0) return;

        float sign = (hand == InteractionHand.MAIN_HAND) ? 1f : -1f;
        matrices.translate(sign * ox, oy, oz);
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V")
    )
    private void fungus$onRenderItem(AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand,
                                     float swingProgress, ItemStack item, float equipProgress, PoseStack matrices,
                                     SubmitNodeCollector queue, int light, CallbackInfo ci) {
        if (!Viewmodel.isActive()) return;
        applyRotAndScale(matrices);
    }

    @Inject(
            method = "renderPlayerArm",
            at = @At("HEAD")
    )
    private void fungus$beforeRenderHand(PoseStack matrices, SubmitNodeCollector queue, int light,
                                         float equipProgress, float swingProgress, HumanoidArm arm, CallbackInfo ci) {
        if (!Viewmodel.isActive() || !Viewmodel.applyToHand) return;

        double ox = Viewmodel.offsetX;
        double oy = Viewmodel.offsetY;
        double oz = Viewmodel.offsetZ;
        if (ox == 0.0 && oy == 0.0 && oz == 0.0) return;

        float sign = (arm == HumanoidArm.RIGHT) ? 1f : -1f;
        matrices.translate(sign * ox, oy, oz);
    }

    @Inject(
            method = "renderPlayerArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;getPlayerRenderer(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;")
    )
    private void fungus$onRenderHand(PoseStack matrices, SubmitNodeCollector queue, int light,
                                     float equipProgress, float swingProgress, HumanoidArm arm, CallbackInfo ci) {
        if (!Viewmodel.isActive() || !Viewmodel.applyToHand) return;
        applyRotAndScale(matrices);
    }

    @WrapOperation(
            method = "swingArm",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 0)
    )
    private void fungus$onSwingArmTranslate(PoseStack stack, float x, float y, float z, Operation<Void> original) {
        if (Viewmodel.isActive()) {
            original.call(stack, x * (float) Viewmodel.swingX, y * (float) Viewmodel.swingY, z * (float) Viewmodel.swingZ);
        } else {
            original.call(stack, x, y, z);
        }
    }

    @Inject(
            method = "shouldInstantlyReplaceVisibleItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fungus$shouldSkipHandAnimationOnSwap(ItemStack from, ItemStack to, CallbackInfoReturnable<Boolean> cir) {
        if (Viewmodel.isActive() && Viewmodel.noEquip) cir.setReturnValue(true);
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void fungus$updateHeldItems(CallbackInfo ci) {
        if (Viewmodel.isActive() && Viewmodel.noEquip) {
            this.mainHandHeight = 1.0f;
            this.offHandHeight = 1.0f;
            this.oMainHandHeight = 1.0f;
            this.oOffHandHeight = 1.0f;
        }
    }

    private static void applyRotAndScale(PoseStack matrices) {
        double rx = Viewmodel.rotX;
        double ry = Viewmodel.rotY;
        double rz = Viewmodel.rotZ;
        if (rx != 0.0 || ry != 0.0 || rz != 0.0) {
            matrices.mulPose(Axis.XP.rotationDegrees((float) rx));
            matrices.mulPose(Axis.YP.rotationDegrees((float) ry));
            matrices.mulPose(Axis.ZP.rotationDegrees((float) rz));
        }
        double sx = Viewmodel.scaleX;
        double sy = Viewmodel.scaleY;
        double sz = Viewmodel.scaleZ;
        if (sx != 1.0 || sy != 1.0 || sz != 1.0) {
            matrices.scale((float) sx, (float) sy, (float) sz);
        }
    }
}
