package io.github.ryn.fungus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.ryn.fungus.feature.Viewmodel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Shadow private float equipProgressMainHand;
    @Shadow private float equipProgressOffHand;
    @Shadow private float lastEquipProgressMainHand;
    @Shadow private float lastEquipProgressOffHand;

    @Inject(
            method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER)
    )
    private void fungus$beforeRenderItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand,
                                         float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                                         OrderedRenderCommandQueue queue, int light, CallbackInfo ci) {
        if (!Viewmodel.isActive()) return;
        if (!Viewmodel.applyToHand && item.isEmpty()) return;

        double ox = Viewmodel.offsetX;
        double oy = Viewmodel.offsetY;
        double oz = Viewmodel.offsetZ;
        if (ox == 0.0 && oy == 0.0 && oz == 0.0) return;

        float sign = (hand == Hand.MAIN_HAND) ? 1f : -1f;
        matrices.translate(sign * ox, oy, oz);
    }

    @Inject(
            method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V")
    )
    private void fungus$onRenderItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand,
                                     float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                                     OrderedRenderCommandQueue queue, int light, CallbackInfo ci) {
        if (!Viewmodel.isActive()) return;
        applyRotAndScale(matrices);
    }

    @Inject(
            method = "renderArmHoldingItem",
            at = @At("HEAD")
    )
    private void fungus$beforeRenderHand(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                                         float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        if (!Viewmodel.isActive() || !Viewmodel.applyToHand) return;

        double ox = Viewmodel.offsetX;
        double oy = Viewmodel.offsetY;
        double oz = Viewmodel.offsetZ;
        if (ox == 0.0 && oy == 0.0 && oz == 0.0) return;

        float sign = (arm == Arm.RIGHT) ? 1f : -1f;
        matrices.translate(sign * ox, oy, oz);
    }

    @Inject(
            method = "renderArmHoldingItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderManager;getPlayerRenderer(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/client/render/entity/PlayerEntityRenderer;")
    )
    private void fungus$onRenderHand(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                                     float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        if (!Viewmodel.isActive() || !Viewmodel.applyToHand) return;
        applyRotAndScale(matrices);
    }

    @WrapOperation(
            method = "swingArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0)
    )
    private void fungus$onSwingArmTranslate(MatrixStack stack, float x, float y, float z, Operation<Void> original) {
        if (Viewmodel.isActive()) {
            original.call(stack, x * (float) Viewmodel.swingX, y * (float) Viewmodel.swingY, z * (float) Viewmodel.swingZ);
        } else {
            original.call(stack, x, y, z);
        }
    }

    @Inject(
            method = "shouldSkipHandAnimationOnSwap",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fungus$shouldSkipHandAnimationOnSwap(ItemStack from, ItemStack to, CallbackInfoReturnable<Boolean> cir) {
        if (Viewmodel.isActive() && Viewmodel.noEquip) cir.setReturnValue(true);
    }

    @Inject(
            method = "updateHeldItems",
            at = @At("TAIL")
    )
    private void fungus$updateHeldItems(CallbackInfo ci) {
        if (Viewmodel.isActive() && Viewmodel.noEquip) {
            this.equipProgressMainHand = 1.0f;
            this.equipProgressOffHand = 1.0f;
            this.lastEquipProgressMainHand = 1.0f;
            this.lastEquipProgressOffHand = 1.0f;
        }
    }

    private static void applyRotAndScale(MatrixStack matrices) {
        double rx = Viewmodel.rotX;
        double ry = Viewmodel.rotY;
        double rz = Viewmodel.rotZ;
        if (rx != 0.0 || ry != 0.0 || rz != 0.0) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) rx));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) ry));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) rz));
        }
        double sx = Viewmodel.scaleX;
        double sy = Viewmodel.scaleY;
        double sz = Viewmodel.scaleZ;
        if (sx != 1.0 || sy != 1.0 || sz != 1.0) {
            matrices.scale((float) sx, (float) sy, (float) sz);
        }
    }
}
