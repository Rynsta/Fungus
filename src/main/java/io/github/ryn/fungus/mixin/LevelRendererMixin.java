package io.github.ryn.fungus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.ryn.fungus.feature.BlockHighlight;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    // Suppress vanilla's block hit-outline (the black wireframe) when the Block Highlight module would
    // draw over it, so the two don't z-fight. If both the fill and outline sub-toggles are off our renderer
    // draws nothing, so we leave the vanilla outline alone. We cancel the draw (renderBlockOutline), not the
    // render-state extract, so vanilla state still populates and only the wireframe paint is skipped.
    @Inject(method = "renderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void fungus$hideVanillaBlockOutline(MultiBufferSource.BufferSource bufferSource,
                                                PoseStack poseStack,
                                                boolean sortTranslucent,
                                                LevelRenderState levelRenderState,
                                                CallbackInfo ci) {
        if (BlockHighlight.enabled && (BlockHighlight.fillEnabled || BlockHighlight.outlineEnabled)) {
            ci.cancel();
        }
    }
}
