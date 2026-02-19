package com.kneelawk.commonevents.events.impl.client;

import net.minecraft.client.renderer.state.LevelRenderState;

import org.jetbrains.annotations.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderContext;

import org.jspecify.annotations.NonNull;

public record LevelRenderContextImpl(LevelRenderer levelRenderer, @Nullable PoseStack poseStack,
                                     Matrix4f modelViewMatrix, LevelRenderState levelState,
                                     @Nullable MultiBufferSource bufferSource)
    implements LevelRenderContext {
    @Override
    public @NonNull LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    @Override
    public @Nullable PoseStack getPoseStack() {
        return poseStack;
    }

    @Override
    public @NonNull LevelRenderState getLevelState() {
        return levelState;
    }

    @Override
    public @Nullable MultiBufferSource getBufferSource() {
        return bufferSource;
    }
}
