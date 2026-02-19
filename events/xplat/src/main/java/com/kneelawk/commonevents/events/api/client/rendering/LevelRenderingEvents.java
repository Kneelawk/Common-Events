package com.kneelawk.commonevents.events.api.client.rendering;

import com.kneelawk.commonevents.api.BusEvent;
import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.api.Scan;
import com.kneelawk.commonevents.mainbus.api.CommonEventsMainBus;

/**
 * Events fired throughout the course of level rendering.
 */
@Scan
public final class LevelRenderingEvents {
    private LevelRenderingEvents() {}

     /**
     * Event fired after the level has been extracted into a render state.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    public static final Event<Extraction> EXTRACTION = Event.createSimple(Extraction.class);

    /**
     * Event fired before entities are rendered.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    public static final Event<BeforeEntities> BEFORE_ENTITIES = Event.createSimple(BeforeEntities.class);

    /**
     * Event fired after entities have been rendered.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    public static final Event<AfterEntities> AFTER_ENTITIES = Event.createSimple(AfterEntities.class);

    /**
     * Event fired after all translucent blocks and particles have been rendered.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    public static final Event<AfterTranslucent> AFTER_TRANSLUCENT = Event.createSimple(AfterTranslucent.class);

    /**
     * Event fired after level rendering has completed.
     */
    @BusEvent(CommonEventsMainBus.NAME)
    public static final Event<End> END = Event.createSimple(End.class);

    /**
     * Fired after the level has been extracted into a render state.
     */
    @FunctionalInterface
    public interface Extraction {
        /**
         * Called after the level has been extracted into a render state.
         *
         * @param ctx the current extraction context.
         */
        void onExtract(LevelExtractionContext ctx);
    }

    /**
     * Fired before entities are rendered.
     */
    @FunctionalInterface
    public interface BeforeEntities {
        /**
         * Called before entities are rendered.
         *
         * @param ctx the current rendering context.
         */
        void beforeEntities(LevelRenderContext ctx);
    }

    /**
     * Fired after entities have been rendered.
     */
    @FunctionalInterface
    public interface AfterEntities {
        /**
         * Called after entities have been rendered.
         *
         * @param ctx the current rendering context.
         */
        void afterEntities(LevelRenderContext ctx);
    }

    /**
     * Fired after all translucent blocks and particles have been rendered.
     */
    @FunctionalInterface
    public interface AfterTranslucent {
        /**
         * Called after all translucent blocks and particles have been rendered.
         *
         * @param ctx the current rendering context.
         */
        void afterTranslucent(LevelRenderContext ctx);
    }

    /**
     * Fired after level rendering has completed.
     */
    @FunctionalInterface
    public interface End {
        /**
         * Called after level rendering has completed.
         *
         * @param ctx the current rendering context.
         */
        void onEnd(LevelRenderContext ctx);
    }
}
