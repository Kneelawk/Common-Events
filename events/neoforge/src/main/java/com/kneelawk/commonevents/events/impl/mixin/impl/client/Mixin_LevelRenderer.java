package com.kneelawk.commonevents.events.impl.mixin.impl.client;

import com.kneelawk.commonevents.events.impl.client.CommonEventsEventsClient;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class Mixin_LevelRenderer {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;"))
    private void captureViewAndProjectionMatrixes(GraphicsResourceAllocator graphicsResourceAllocator,
                                                  DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera,
                                                  Matrix4f frustumMatrix, Matrix4f projectionMatrix,
                                                  Matrix4f cullingProjectionMatrix, GpuBufferSlice shaderFog,
                                                  Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        CommonEventsEventsClient.MATRICES.set(new CommonEventsEventsClient.ExtractionMatrices(frustumMatrix, projectionMatrix, cullingProjectionMatrix));
    }
}
