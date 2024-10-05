package com.kneelawk.commonevents.events.impl.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderContext;
import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderingEvents;

public class CommonEventsEventsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.BEFORE_ENTITIES.register(
            ctx -> LevelRenderingEvents.BEFORE_ENTITIES.invoker().beforeEntities(convert(ctx)));
        WorldRenderEvents.AFTER_ENTITIES.register(
            ctx -> LevelRenderingEvents.AFTER_ENTITIES.invoker().afterEntities(convert(ctx)));
        WorldRenderEvents.AFTER_TRANSLUCENT.register(
            ctx -> LevelRenderingEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(convert(ctx)));
        WorldRenderEvents.END.register(ctx -> LevelRenderingEvents.END.invoker().onEnd(convert(ctx)));
    }

    private static LevelRenderContext convert(WorldRenderContext ctx) {
        return new LevelRenderContextImpl(ctx.worldRenderer(), ctx.matrixStack(), ctx.positionMatrix(),
            ctx.projectionMatrix(), ctx.tickCounter(), ctx.camera(), ctx.frustum(), ctx.consumers());
    }
}
