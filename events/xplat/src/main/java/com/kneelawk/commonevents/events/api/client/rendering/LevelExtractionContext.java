package com.kneelawk.commonevents.events.api.client.rendering;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.culling.Frustum;

import net.minecraft.client.renderer.state.LevelRenderState;

public interface LevelExtractionContext {
    /**
     * {@return the level render state}
     */
    LevelRenderState getLevelState();

    /**
     * {@return the current camera}
     */
    Camera getCamera();

    /**
     * {@return the current frustum}
     */
    Frustum getFrustum();

    /**
     * {@return the current delta tracker}
     */
    DeltaTracker getDeltaTracker();
}
