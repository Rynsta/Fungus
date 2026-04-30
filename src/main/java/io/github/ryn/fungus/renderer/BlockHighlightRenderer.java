package io.github.ryn.fungus.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.ryn.fungus.feature.BlockHighlight;
import io.github.ryn.fungus.mixin.CameraAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.List;

public class BlockHighlightRenderer {

    private static final float OFFSET = 0.002f;

    private static final RenderPipeline HIGHLIGHT_FILL_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withLocation("fungus/block_highlight_fill")
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
                    .build()
    );

    private static final RenderLayer HIGHLIGHT_FILL_LAYER = RenderLayer.of(
            "fungus_block_highlight_fill",
            RenderSetup.builder(HIGHLIGHT_FILL_PIPELINE)
                    .expectedBufferSize(1536)
                    .translucent()
                    .build()
    );

    public static void register() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(BlockHighlightRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        if (!BlockHighlight.enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        HitResult hitResult = client.crosshairTarget;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos pos = blockHit.getBlockPos();

        Vec3d cameraPos = ((CameraAccessor) client.gameRenderer.getCamera()).fungus$getPos();
        MatrixStack matrices = context.matrices();
        VertexConsumerProvider vertexConsumers = context.consumers();
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

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        if (BlockHighlight.wrapEnabled) {
            BlockState state = client.world.getBlockState(pos);
            VoxelShape shape = state.getOutlineShape(client.world, pos, ShapeContext.of(client.player));
            List<Box> boxes = shape.getBoundingBoxes();
            if (boxes.isEmpty()) {
                matrices.pop();
                return;
            }

            if (BlockHighlight.fillEnabled) {
                for (Box box : boxes) {
                    float x1 = (float)(pos.getX() + box.minX) - OFFSET;
                    float y1 = (float)(pos.getY() + box.minY) - OFFSET;
                    float z1 = (float)(pos.getZ() + box.minZ) - OFFSET;
                    float x2 = (float)(pos.getX() + box.maxX) + OFFSET;
                    float y2 = (float)(pos.getY() + box.maxY) + OFFSET;
                    float z2 = (float)(pos.getZ() + box.maxZ) + OFFSET;
                    drawFilledBox(vertexConsumers, matrices.peek(), x1, y1, z1, x2, y2, z2, fillR, fillG, fillB, fillA);
                }
            }

            if (BlockHighlight.outlineEnabled) {
                VertexConsumer lineBuffer = vertexConsumers.getBuffer(RenderLayers.LINES);
                MatrixStack.Entry entry = matrices.peek();
                final float fr = outR, fg = outG, fb = outB, fa = outA;
                Box bb = shape.getBoundingBox();
                shape.forEachEdge((x1e, y1e, z1e, x2e, y2e, z2e) ->
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
                drawFilledBox(vertexConsumers, matrices.peek(), x1, y1, z1, x2, y2, z2, fillR, fillG, fillB, fillA);
            }
            if (BlockHighlight.outlineEnabled) {
                VertexConsumer lineBuffer = vertexConsumers.getBuffer(RenderLayers.LINES);
                drawBoxOutline(lineBuffer, matrices.peek(), x1, y1, z1, x2, y2, z2, outR, outG, outB, outA);
            }
        }

        matrices.pop();
    }

    private static void drawFilledBox(VertexConsumerProvider consumers, MatrixStack.Entry matrix,
                                      float x1, float y1, float z1, float x2, float y2, float z2,
                                      float r, float g, float b, float a) {
        VertexConsumer buf = consumers.getBuffer(HIGHLIGHT_FILL_LAYER);

        vert(buf, matrix, x1, y1, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z2, r, g, b, a);
        vert(buf, matrix, x1, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z1, r, g, b, a);

        vert(buf, matrix, x2, y1, z1, r, g, b, a);
        vert(buf, matrix, x1, y1, z1, r, g, b, a);
        vert(buf, matrix, x2, y2, z1, r, g, b, a);
        vert(buf, matrix, x1, y2, z1, r, g, b, a);
        vert(buf, matrix, x1, y2, z1, r, g, b, a);
        vert(buf, matrix, x1, y1, z1, r, g, b, a);

        vert(buf, matrix, x1, y1, z1, r, g, b, a);
        vert(buf, matrix, x2, y1, z1, r, g, b, a);
        vert(buf, matrix, x1, y1, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z2, r, g, b, a);
        vert(buf, matrix, x1, y2, z2, r, g, b, a);

        vert(buf, matrix, x1, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y2, z2, r, g, b, a);
        vert(buf, matrix, x1, y2, z1, r, g, b, a);
        vert(buf, matrix, x2, y2, z1, r, g, b, a);
        vert(buf, matrix, x2, y2, z1, r, g, b, a);
        vert(buf, matrix, x1, y1, z1, r, g, b, a);

        vert(buf, matrix, x1, y1, z1, r, g, b, a);
        vert(buf, matrix, x1, y1, z2, r, g, b, a);
        vert(buf, matrix, x1, y2, z1, r, g, b, a);
        vert(buf, matrix, x1, y2, z2, r, g, b, a);
        vert(buf, matrix, x1, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z2, r, g, b, a);

        vert(buf, matrix, x2, y1, z2, r, g, b, a);
        vert(buf, matrix, x2, y1, z1, r, g, b, a);
        vert(buf, matrix, x2, y2, z2, r, g, b, a);
        vert(buf, matrix, x2, y2, z1, r, g, b, a);
    }

    private static void vert(VertexConsumer buf, MatrixStack.Entry matrix,
                             float x, float y, float z, float r, float g, float b, float a) {
        buf.vertex(matrix, x, y, z).color(r, g, b, a);
    }

    private static void drawBoxOutline(VertexConsumer buf, MatrixStack.Entry matrix,
                                       float x1, float y1, float z1, float x2, float y2, float z2,
                                       float r, float g, float b, float a) {
        drawLine(buf, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
        drawLine(buf, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
        drawLine(buf, matrix, x2, y1, z2, x1, y1, z2, r, g, b, a);
        drawLine(buf, matrix, x1, y1, z2, x1, y1, z1, r, g, b, a);

        drawLine(buf, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
        drawLine(buf, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
        drawLine(buf, matrix, x2, y2, z2, x1, y2, z2, r, g, b, a);
        drawLine(buf, matrix, x1, y2, z2, x1, y2, z1, r, g, b, a);

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

    private static void drawLine(VertexConsumer buf, MatrixStack.Entry matrix,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 float r, float g, float b, float a) {
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6f) len = 1.0f;
        float nx = dx / len, ny = dy / len, nz = dz / len;

        buf.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(matrix, nx, ny, nz).lineWidth(1.0f);
        buf.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(matrix, nx, ny, nz).lineWidth(1.0f);
    }
}
