package com.kneelawk.commonevents.events.api.client.rendering;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.culling.Frustum;

import org.joml.Matrix4fc;

public interface LevelExtractionContext {
    Camera getCamera();

    Frustum getFrustum();

    DeltaTracker getDeltaTracker();

    Matrix4fc getViewMatrix();

    Matrix4fc getProjectionMatrix();

    Matrix4fc getCullProjectionMatrix();
}
