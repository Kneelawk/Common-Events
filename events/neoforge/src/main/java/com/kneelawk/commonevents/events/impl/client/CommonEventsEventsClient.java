package com.kneelawk.commonevents.events.impl.client;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.IRenderableSection;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import net.minecraft.client.Minecraft;

import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderContext;
import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderingEvents;
import com.kneelawk.commonevents.events.impl.CEEConstants;

@EventBusSubscriber(modid = CEEConstants.MOD_ID, value = Dist.CLIENT)
public class CommonEventsEventsClient {
    @SubscribeEvent
    public static void onRenderLevelBeforeEntities(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        LevelRenderingEvents.BEFORE_ENTITIES.invoker().beforeEntities(convert(event));
    }

    @SubscribeEvent
    public static void onRenderLevelAfterEntities(RenderLevelStageEvent.AfterEntities event) {
        LevelRenderingEvents.AFTER_ENTITIES.invoker().afterEntities(convert(event));
    }

    @SubscribeEvent
    public static void onRenderLevelAfterTranslucent(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        LevelRenderingEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(convert(event));
    }

    @SubscribeEvent
    public static void onRenderLevelEnd(RenderLevelStageEvent.AfterLevel event) {
        LevelRenderingEvents.END.invoker().onEnd(convert(event));
    }

    private static LevelRenderContext convert(RenderLevelStageEvent event) {
        ObjectArrayList<SectionRenderDispatcher.RenderSection> renderSections = new ObjectArrayList<>();
        for (IRenderableSection section : event.getRenderableSections()) {
            if (section instanceof SectionRenderDispatcher.RenderSection renderSection) {
                renderSections.add(renderSection);
            }
        }

        return new LevelRenderContextImpl(event.getLevelRenderer(), event.getPoseStack(), event.getModelViewMatrix(),
            event.getLevelRenderState(), renderSections, Minecraft.getInstance().renderBuffers().bufferSource());
    }
}
