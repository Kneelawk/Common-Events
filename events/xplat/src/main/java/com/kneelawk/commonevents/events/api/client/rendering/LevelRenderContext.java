package com.kneelawk.commonevents.events.api.client.rendering;

import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.state.LevelRenderState;

import org.jetbrains.annotations.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;

/**
 * Holds context about rendering a level.
 */
public interface LevelRenderContext {
    /**
     * {@return the level renderer instance}
     */
    LevelRenderer getLevelRenderer();

    /**
     * {@return the current pose stack}
     * <p>
     * This is null before {@link LevelRenderingEvents#AFTER_ENTITIES}.
     */
    @Nullable PoseStack getPoseStack();

    /**
     * {@return the level render state}
     */
    LevelRenderState getLevelState();

    /**
     * {@return an iterable of the render sections}
     */
    Iterable<SectionRenderDispatcher.RenderSection> getRenderSections();

    /**
     * Gets the buffer source used by the level renderer for most non-terrain renders.
     * <p>
     * This buffer may be {@code null} or just unusable after {@link LevelRenderingEvents#AFTER_ENTITIES}, in which
     * case, one should just draw to the framebuffer directly.
     *
     * @return the buffer source used by the level renderer.
     */
    @Nullable MultiBufferSource getBufferSource();
}
