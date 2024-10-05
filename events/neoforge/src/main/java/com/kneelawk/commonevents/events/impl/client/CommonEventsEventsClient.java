package com.kneelawk.commonevents.events.impl.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import net.minecraft.client.Minecraft;

import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderContext;
import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderingEvents;
import com.kneelawk.commonevents.events.impl.CEEConstants;

@EventBusSubscriber(modid = CEEConstants.MOD_ID, value = Dist.CLIENT)
public class CommonEventsEventsClient {
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            LevelRenderingEvents.BEFORE_ENTITIES.invoker().beforeEntities(convert(event));
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            LevelRenderingEvents.AFTER_ENTITIES.invoker().afterEntities(convert(event));
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            LevelRenderingEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(convert(event));
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            LevelRenderingEvents.END.invoker().onEnd(convert(event));
        }
    }

    private static LevelRenderContext convert(RenderLevelStageEvent event) {
        return new LevelRenderContextImpl(event.getLevelRenderer(), event.getPoseStack(), event.getModelViewMatrix(),
            event.getProjectionMatrix(), event.getPartialTick(), event.getCamera(), event.getFrustum(),
            Minecraft.getInstance().renderBuffers().bufferSource());
    }
}
