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

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.api.Listen;
import com.kneelawk.commonevents.api.Listener;

public class CommonEventsExample {
    public static final String MOD_ID = "common_events_example";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    static {
        LOGGER.info("# Creating MY_EVENT...");
    }

    public static Event<MyCallback> MY_EVENT = Event.createWithPhases(MyCallback.class, callbacks -> () -> {
        for (MyCallback callback : callbacks) {
            callback.onEvent();
        }
    }, new ResourceLocation(MOD_ID, "my_phase"), Event.DEFAULT_PHASE);

    static {
        LOGGER.info("# MY_EVENT created.");
    }

    public static void init() {
        MY_EVENT.invoker().onEvent();
    }

    @Listener
    public class EventListener {
        static {
            LOGGER.info("# EventListener statically initialized");
        }

        @Listen(value = MyCallback.class, phase = "common_events_example:my_phase")
        public static void onEvent() {
            LOGGER.info("> onEvent received");
        }
    }

    @FunctionalInterface
    public interface MyCallback {
        void onEvent();
    }
}
