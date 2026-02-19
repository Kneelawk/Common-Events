package com.kneelawk.commonevents.events.impl.client;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.ClientModInitializer;

import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderContext;
import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderingEvents;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

public class CommonEventsEventsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.BEFORE_ENTITIES.register(
            ctx -> LevelRenderingEvents.BEFORE_ENTITIES.invoker().beforeEntities(convert(ctx)));
        WorldRenderEvents.AFTER_ENTITIES.register(
            ctx -> LevelRenderingEvents.AFTER_ENTITIES.invoker().afterEntities(convert(ctx)));
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(
            (ctx, outlineState) -> {
                LevelRenderingEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(convert(ctx));
                return true;
            });
        WorldRenderEvents.END_MAIN.register(ctx -> LevelRenderingEvents.END.invoker().onEnd(convert(ctx)));
    }

    private static LevelRenderContext convert(WorldRenderContext ctx) {
        return new LevelRenderContextImpl(ctx.worldRenderer(), ctx.matrices(), RenderSystem.getModelViewMatrix(),
            ctx.worldState(), ctx.worldRenderer().getVisibleSections(), ctx.consumers());
    }
}
