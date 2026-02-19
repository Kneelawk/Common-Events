package com.kneelawk.commonevents.events.impl.client;

import com.kneelawk.commonevents.events.api.client.rendering.LevelExtractionContext;
import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderContext;

import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;

import org.jetbrains.annotations.Nullable;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public record LevelExtractionContextImpl(Camera camera, Frustum frustum, DeltaTracker deltaTracker,
                                         Matrix4fc viewMatrix, Matrix4fc projectionMatrix,
                                         Matrix4fc cullProjectionMatrix)
    implements LevelExtractionContext {
    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    public Frustum getFrustum() {
        return frustum;
    }

    @Override
    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }

    @Override
    public Matrix4fc getViewMatrix() {
        return viewMatrix;
    }

    @Override
    public Matrix4fc getProjectionMatrix() {
        return projectionMatrix;
    }

    @Override
    public Matrix4fc getCullProjectionMatrix() {
        return cullProjectionMatrix;
    }
}
