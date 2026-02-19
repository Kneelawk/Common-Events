package com.kneelawk.commonevents.events.impl.client;

import com.kneelawk.commonevents.events.api.client.rendering.LevelExtractionContext;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;

public record LevelExtractionContextImpl(LevelRenderState levelRenderState, Camera camera,
                                         Frustum frustum, DeltaTracker deltaTracker)
    implements LevelExtractionContext {
    @Override
    public LevelRenderState getLevelState() {
        return levelRenderState;
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
    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }
}
