package com.kneelawk.commonevents.events.impl.client;

import com.kneelawk.commonevents.events.api.client.rendering.LevelExtractionContext;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.ClientModInitializer;

import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderContext;
import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderingEvents;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

public class CommonEventsEventsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.END_EXTRACTION.register(
            ctx -> LevelRenderingEvents.EXTRACTION.invoker().onExtract(convertExtraction(ctx))
        );
        WorldRenderEvents.BEFORE_ENTITIES.register(
            ctx -> LevelRenderingEvents.BEFORE_ENTITIES.invoker().beforeEntities(convertRender(ctx)));
        WorldRenderEvents.AFTER_ENTITIES.register(
            ctx -> LevelRenderingEvents.AFTER_ENTITIES.invoker().afterEntities(convertRender(ctx)));
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(
            (ctx, outlineState) -> {
                LevelRenderingEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(convertRender(ctx));
                return true;
            });
        WorldRenderEvents.END_MAIN.register(ctx -> LevelRenderingEvents.END.invoker().onEnd(convertRender(ctx)));
    }

    private static LevelExtractionContext convertExtraction(WorldExtractionContext ctx) {
        return new LevelExtractionContextImpl(ctx.worldState(), ctx.camera(),
            ctx.frustum(), ctx.tickCounter());
    }

    private static LevelRenderContext convertRender(WorldRenderContext ctx) {
        return new LevelRenderContextImpl(ctx.worldRenderer(), ctx.matrices(), RenderSystem.getModelViewMatrix(),
            ctx.worldState(), ctx.consumers());
    }
}
