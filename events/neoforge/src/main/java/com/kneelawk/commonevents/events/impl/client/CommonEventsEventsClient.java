package com.kneelawk.commonevents.events.impl.client;

import com.kneelawk.commonevents.events.api.client.rendering.LevelExtractionContext;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.IRenderableSection;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import net.minecraft.client.Minecraft;

import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderContext;
import com.kneelawk.commonevents.events.api.client.rendering.LevelRenderingEvents;
import com.kneelawk.commonevents.events.impl.CEEConstants;

import org.joml.Matrix4fc;

@EventBusSubscriber(modid = CEEConstants.MOD_ID, value = Dist.CLIENT)
public class CommonEventsEventsClient {
    @SubscribeEvent
    public static void onExtractLevel(ExtractLevelRenderStateEvent event) {
        LevelRenderingEvents.EXTRACTION.invoker().onExtract(convertExtraction(event));
    }

    @SubscribeEvent
    public static void onRenderLevelBeforeEntities(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        LevelRenderingEvents.BEFORE_ENTITIES.invoker().beforeEntities(convertRender(event));
    }

    @SubscribeEvent
    public static void onRenderLevelAfterEntities(RenderLevelStageEvent.AfterEntities event) {
        LevelRenderingEvents.AFTER_ENTITIES.invoker().afterEntities(convertRender(event));
    }

    @SubscribeEvent
    public static void onRenderLevelAfterTranslucent(RenderLevelStageEvent.AfterParticles event) {
        LevelRenderingEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(convertRender(event));
    }

    @SubscribeEvent
    public static void onRenderLevelEnd(RenderLevelStageEvent.AfterLevel event) {
        LevelRenderingEvents.END.invoker().onEnd(convertRender(event));
    }

    public record ExtractionMatrices(Matrix4fc view, Matrix4fc projection, Matrix4fc cullProjection) {

    }

    private static LevelExtractionContext convertExtraction(ExtractLevelRenderStateEvent event) {
        return new LevelExtractionContextImpl(event.getRenderState(), event.getCamera(),
            event.getFrustum(), event.getDeltaTracker());
    }

    private static LevelRenderContext convertRender(RenderLevelStageEvent event) {
        return new LevelRenderContextImpl(event.getLevelRenderer(), event.getPoseStack(), event.getModelViewMatrix(),
            event.getLevelRenderState(), Minecraft.getInstance().renderBuffers().bufferSource());
    }
}
