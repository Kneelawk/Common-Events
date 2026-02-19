package com.kneelawk.commonevents.events.impl.client;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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

import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderContext;

public record LevelRenderContextImpl(LevelRenderer levelRenderer, @Nullable PoseStack poseStack,
                                     Matrix4f modelViewMatrix, LevelRenderState levelState,
                                     ObjectArrayList<SectionRenderDispatcher.RenderSection> renderSections,
                                     @Nullable MultiBufferSource bufferSource)
    implements LevelRenderContext {
    @Override
    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    @Override
    public PoseStack getPoseStack() {
        return poseStack;
    }

    @Override
    public LevelRenderState getLevelState() {
        return levelState;
    }

    @Override
    public Iterable<SectionRenderDispatcher.RenderSection> getRenderSections() {
        return renderSections;
    }

    @Override
    public @Nullable MultiBufferSource getBufferSource() {
        return bufferSource;
    }
}
