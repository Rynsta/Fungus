package io.github.ryn.fungus.mixin;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code RenderType.create} is package-private in 26.1 — this invoker lets the renderer build a
 * custom render type for the block-highlight fill/outline geometry.
 */
@Mixin(RenderType.class)
public interface RenderTypeInvoker {
    @Invoker("create")
    static RenderType invokeCreate(String name, RenderSetup setup) {
        throw new AssertionError();
    }
}
