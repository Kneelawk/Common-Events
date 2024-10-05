package com.kneelawk.commonevents.events.impl.client;

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
                                     Matrix4f modelViewMatrix, Matrix4f projectionMatrix, DeltaTracker deltaTracker,
                                     Camera camera, Frustum frustum, @Nullable MultiBufferSource bufferSource)
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
    public Matrix4f getModelViewMatrix() {
        return modelViewMatrix;
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    @Override
    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    public Frustum getFrustum() {
        return frustum;
    }

    @Override
    public @Nullable MultiBufferSource getBufferSource() {
        return bufferSource;
    }
}
