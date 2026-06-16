package io.github.ryn.fungus.renderer;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.ryn.fungus.feature.BlockHighlight;
import io.github.ryn.fungus.mixin.RenderTypeInvoker;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Optional;

public class BlockHighlightRenderer {

    private static final float OFFSET = 0.002f;

    private static RenderPipeline.Snippet snippetFrom(RenderPipeline base) {
        return new RenderPipeline.Snippet(
                Optional.of(base.getVertexShader()), Optional.of(base.getFragmentShader()),
                Optional.of(base.getShaderDefines()), Optional.of(base.getSamplers()),
                Optional.of(base.getUniforms()), Optional.of(base.getColorTargetState()),
                Optional.of(base.getDepthStencilState()), Optional.of(base.getPolygonMode()),
                Optional.of(base.isCull()), Optional.of(base.getVertexFormat()),
                Optional.of(base.getVertexFormatMode()));
    }

    private static final RenderPipeline HIGHLIGHT_FILL_PIPELINE = RenderPipeline.builder(snippetFrom(RenderPipelines.DEBUG_FILLED_BOX))
            .withLocation("fungus/block_highlight_fill")
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .build();

    private static final RenderType HIGHLIGHT_FILL_LAYER = RenderTypeInvoker.invokeCreate(
            "fungus_block_highlight_fill",
            RenderSetup.builder(HIGHLIGHT_FILL_PIPELINE).bufferSize(1536).sortOnUpload().createRenderSetup()
    );

    public static void register() {
        LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(BlockHighlightRenderer::render);
    }

    private static void render(LevelRenderContext context) {
        if (!BlockHighlight.enabled) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        HitResult hitResult = client.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos pos = blockHit.getBlockPos();

        Vec3 cameraPos = client.gameRenderer.getMainCamera().position();
        PoseStack matrices = context.poseStack();
        MultiBufferSource vertexConsumers = context.bufferSource();
        if (vertexConsumers == null) return;

        float fillR = 0, fillG = 0, fillB = 0, fillA = 0;
        if (BlockHighlight.fillEnabled) {
            int c = BlockHighlight.fillColor;
            fillR = ((c >> 16) & 0xFF) / 255.0f;
            fillG = ((c >>  8) & 0xFF) / 255.0f;
            fillB = ( c        & 0xFF) / 255.0f;
            fillA = ((c >> 24) & 0xFF) / 255.0f;
        }

        float outR = 0, outG = 0, outB = 0, outA = 0;
        if (BlockHighlight.outlineEnabled) {
            int c = BlockHighlight.outlineColor;
            outR = ((c >> 16) & 0xFF) / 255.0f;
            outG = ((c >>  8) & 0xFF) / 255.0f;
            outB = ( c        & 0xFF) / 255.0f;
            outA = ((c >> 24) & 0xFF) / 255.0f;
        }

        matrices.pushPose();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        if (BlockHighlight.wrapEnabled) {
            BlockState state = client.level.getBlockState(pos);
            VoxelShape shape = state.getShape(client.level, pos, CollisionContext.of(client.player));
            List<AABB> boxes = shape.toAabbs();
            if (boxes.isEmpty()) {
                matrices.popPose();
                return;
            }

            if (BlockHighlight.fillEnabled) {
                for (AABB box : boxes) {
                    float x1 = (float)(pos.getX() + box.minX) - OFFSET;
                    float y1 = (float)(pos.getY() + box.minY) - OFFSET;
                    float z1 = (float)(pos.getZ() + box.minZ) - OFFSET;
                    float x2 = (float)(pos.getX() + box.maxX) + OFFSET;
                    float y2 = (float)(pos.getY() + box.maxY) + OFFSET;
                    float z2 = (float)(pos.getZ() + box.maxZ) + OFFSET;
                    drawFilledBox(vertexConsumers, matrices.last(), x1, y1, z1, x2, y2, z2, fillR, fillG, fillB, fillA);
                }
            }

            if (BlockHighlight.outlineEnabled) {
                VertexConsumer lineBuffer = vertexConsumers.getBuffer(HIGHLIGHT_FILL_LAYER);
                PoseStack.Pose entry = matrices.last();
                final float fr = outR, fg = outG, fb = outB, fa = outA;
                AABB bb = shape.bounds();
                shape.forAllEdges((x1e, y1e, z1e, x2e, y2e, z2e) ->
                        drawLine(lineBuffer, entry,
                                (float)(pos.getX() + nudge(x1e, bb.minX, bb.maxX)),
                                (float)(pos.getY() + nudge(y1e, bb.minY, bb.maxY)),
                                (float)(pos.getZ() + nudge(z1e, bb.minZ, bb.maxZ)),
                                (float)(pos.getX() + nudge(x2e, bb.minX, bb.maxX)),
                                (float)(pos.getY() + nudge(y2e, bb.minY, bb.maxY)),
                                (float)(pos.getZ() + nudge(z2e, bb.minZ, bb.maxZ)),
                                fr, fg, fb, fa)
                );
            }
        } else {
            float x1 = pos.getX() - OFFSET;
            float y1 = pos.getY() - OFFSET;
            float z1 = pos.getZ() - OFFSET;
            float x2 = pos.getX() + 1 + OFFSET;
            float y2 = pos.getY() + 1 + OFFSET;
            float z2 = pos.getZ() + 1 + OFFSET;

            if (BlockHighlight.fillEnabled) {
                drawFilledBox(vertexConsumers, matrices.last(), x1, y1, z1, x2, y2, z2, fillR, fillG, fillB, fillA);
            }
            if (BlockHighlight.outlineEnabled) {
                VertexConsumer lineBuffer = vertexConsumers.getBuffer(HIGHLIGHT_FILL_LAYER);
                drawBoxOutline(lineBuffer, matrices.last(), x1, y1, z1, x2, y2, z2, outR, outG, outB, outA);
            }
        }

        matrices.popPose();
    }

    private static void drawFilledBox(MultiBufferSource consumers, PoseStack.Pose matrix,
                                      float x1, float y1, float z1, float x2, float y2, float z2,
                                      float r, float g, float b, float a) {
        VertexConsumer buf = consumers.getBuffer(HIGHLIGHT_FILL_LAYER);

        // South face (+z)
        vert(buf, matrix, x1, y1, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z2, r, g, b, a);
        vert(buf, matrix, x1, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z1, r, g, b, a);

        // North face (-z)
        vert(buf, matrix, x2, y1, z1, r, g, b, a);
        vert(buf, matrix, x1, y1, z1, r, g, b, a);
        vert(buf, matrix, x2, y2, z1, r, g, b, a);
        vert(buf, matrix, x1, y2, z1, r, g, b, a);
        vert(buf, matrix, x1, y2, z1, r, g, b, a);
        vert(buf, matrix, x1, y1, z1, r, g, b, a);

        // Bottom face (-y)
        vert(buf, matrix, x1, y1, z1, r, g, b, a);
        vert(buf, matrix, x2, y1, z1, r, g, b, a);
        vert(buf, matrix, x1, y1, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z2, r, g, b, a);
        vert(buf, matrix, x1, y2, z2, r, g, b, a);

        // Top face (+y)
        vert(buf, matrix, x1, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y2, z2, r, g, b, a);
        vert(buf, matrix, x1, y2, z1, r, g, b, a);
        vert(buf, matrix, x2, y2, z1, r, g, b, a);
        vert(buf, matrix, x2, y2, z1, r, g, b, a);
        vert(buf, matrix, x1, y1, z1, r, g, b, a);

        // West face (-x)
        vert(buf, matrix, x1, y1, z1, r, g, b, a);
        vert(buf, matrix, x1, y1, z2, r, g, b, a);
        vert(buf, matrix, x1, y2, z1, r, g, b, a);
        vert(buf, matrix, x1, y2, z2, r, g, b, a);
        vert(buf, matrix, x1, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z2, r, g, b, a);

        // East face (+x)
        vert(buf, matrix, x2, y1, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z1, r, g, b, a);
        vert(buf, matrix, x2, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y2, z1, r, g, b, a);
        // Trailing degenerate: isolates this box from whatever follows in the shared strip
        // (outline edges, or the next box in wrap mode), so no stray bridge triangle.
        vert(buf, matrix, x2, y2, z1, r, g, b, a);
    }

    private static void vert(VertexConsumer buf, PoseStack.Pose matrix,
                             float x, float y, float z, float r, float g, float b, float a) {
        buf.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }

    private static void drawBoxOutline(VertexConsumer buf, PoseStack.Pose matrix,
                                       float x1, float y1, float z1, float x2, float y2, float z2,
                                       float r, float g, float b, float a) {
        // Bottom face edges
        drawLine(buf, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
        drawLine(buf, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
        drawLine(buf, matrix, x2, y1, z2, x1, y1, z2, r, g, b, a);
        drawLine(buf, matrix, x1, y1, z2, x1, y1, z1, r, g, b, a);

        // Top face edges
        drawLine(buf, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
        drawLine(buf, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
        drawLine(buf, matrix, x2, y2, z2, x1, y2, z2, r, g, b, a);
        drawLine(buf, matrix, x1, y2, z2, x1, y2, z1, r, g, b, a);

        // Vertical edges
        drawLine(buf, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a);
        drawLine(buf, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
        drawLine(buf, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
        drawLine(buf, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private static double nudge(double val, double min, double max) {
        if (Math.abs(val - min) < 0.001) return val - OFFSET;
        if (Math.abs(val - max) < 0.001) return val + OFFSET;
        return val;
    }

    private static final float LINE_THICKNESS = 0.01f;

    /**
     * Draws a single edge as a thin quad in TRIANGLE_STRIP order through the custom POSITION_COLOR layer
     * (the vanilla LINES render type needs a per-vertex line-width element our VertexConsumer cannot supply,
     * so we expand edges into geometry instead). Leading/trailing degenerate vertices isolate each edge.
     */
    private static void drawLine(VertexConsumer buf, PoseStack.Pose matrix,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 float r, float g, float b, float a) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;

        float ox = 0f, oy = 0f, oz = 0f;
        float d = LINE_THICKNESS;
        if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > Math.abs(dz)) {
            oy = d;
        } else if (Math.abs(dy) >= Math.abs(dx) && Math.abs(dy) > Math.abs(dz)) {
            ox = d;
        } else {
            oy = d;
        }

        buf.addVertex(matrix, x1 - ox, y1 - oy, z1 - oz).setColor(r, g, b, a);
        buf.addVertex(matrix, x1 - ox, y1 - oy, z1 - oz).setColor(r, g, b, a);
        buf.addVertex(matrix, x2 - ox, y2 - oy, z2 - oz).setColor(r, g, b, a);
        buf.addVertex(matrix, x1 + ox, y1 + oy, z1 + oz).setColor(r, g, b, a);
        buf.addVertex(matrix, x2 + ox, y2 + oy, z2 + oz).setColor(r, g, b, a);
        buf.addVertex(matrix, x2 + ox, y2 + oy, z2 + oz).setColor(r, g, b, a);
    }
}
