package com.kneelawk.commonevents.events.impl.mixin.impl.client;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface Invoker_GameRenderer {
    @Invoker("getFov")
    float invokeGetFov(Camera camera, float partialTick, boolean useFovSetting);
}
