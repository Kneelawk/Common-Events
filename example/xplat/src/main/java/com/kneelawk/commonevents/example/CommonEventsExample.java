/*
 * Copyright (c) 2024 Cyan Kneelawk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kneelawk.commonevents.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.BusEvent;
import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.api.EventBus;
import com.kneelawk.commonevents.api.Listen;
import com.kneelawk.commonevents.api.Scan;

public class CommonEventsExample {
    public static final String MOD_ID = "common_events_example";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    static {
        LOGGER.info("# Creating EVENT_BUS...");
    }

    public static final EventBus EVENT_BUS =
        EventBus.builder(ResourceLocation.fromNamespaceAndPath(MOD_ID, "bus")).build();

    static {
        LOGGER.info("# EVENT_BUS created.");
    }

    public static void init() {
        EVENT_BUS.registerListeners(EventListener2.class);
        EventListener3 listener3 = new EventListener3();
        EVENT_BUS.registerListeners(listener3);
        EventHolder.init();
        EventHolder2.init();
    }

    @Scan
    public static class EventHolder {
        static {
            LOGGER.info("# Creating MY_EVENT...");
        }

        // This event is initialized when EVENT_BUS is constructed, to make sure this event can collect listeners
        // registered to the event bus.
        @BusEvent("common_events_example:bus")
        public static Event<MyCallback> MY_EVENT = Event.createWithPhases(MyCallback.class, callbacks -> () -> {
            for (MyCallback callback : callbacks) {
                callback.onEvent();
            }
        }, ResourceLocation.fromNamespaceAndPath(MOD_ID, "my_phase"), Event.DEFAULT_PHASE);

        static {
            LOGGER.info("# MY_EVENT created.");
        }

        public static void init() {
            LOGGER.info("  Firing MY_EVENT...");
            EventHolder.MY_EVENT.invoker().onEvent();
            LOGGER.info("  MY_EVENT fired.");
        }
    }

    @Scan
    public static class EventHolder2 {
        static {
            LOGGER.info("# Creating SIMPLE_EVENT...");
        }

        @BusEvent("common_events_example:bus")
        public static Event<MyCallback2> SIMPLE_EVENT = Event.createSimple(MyCallback2.class);

        static {
            LOGGER.info("# SIMPLE_EVENT created.");
        }

        public static void init() {
            LOGGER.info("  Firing SIMPLE_EVENT...");
            SIMPLE_EVENT.invoker().onOtherEvent("test", Long.MAX_VALUE);
            LOGGER.info("  SIMPLE_EVENT fired.");
        }
    }

    @Scan
    public static class EventListener {
        static {
            LOGGER.info("# EventListener statically initialized");
        }

        @Listen(value = MyCallback.class, phase = "common_events_example:my_phase")
        public static void onEvent() {
            LOGGER.info("> onEvent received in EventListener");
        }

        @Listen(MyCallback2.class)
        public static void onOtherEvent(String str, long l) {
            LOGGER.info("> onOtherEvent received in EventListener: {}, {}", str, l);
        }
    }

    public static class EventListener2 {
        @Listen(MyCallback.class)
        public static void onEvent() {
            LOGGER.info("> onEvent received in EventListener 2");
        }

        @Listen(MyCallback2.class)
        public static void onOtherEvent(String str, long l) {
            LOGGER.info("> onOtherEvent received in EventListener 2: {}, {}", str, l);
        }
    }

    public static class EventListener3 {
        @Listen(MyCallback.class)
        public void onEvent() {
            LOGGER.info("> onEvent received in EventListener 3");
        }

        @Listen(MyCallback2.class)
        public void onOtherEvent(String str, long l) {
            LOGGER.info("> onOtherEvent received in EventListener 3: {}, {}", str, l);
        }
    }

    @FunctionalInterface
    public interface MyCallback {
        void onEvent();
    }

    @FunctionalInterface
    public interface MyCallback2 {
        void onOtherEvent(String str, long l);
    }
}
