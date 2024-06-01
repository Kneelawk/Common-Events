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

package com.kneelawk.commonevents.events.api;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.EventBus;

/**
 * Common Events Events API Root Class.
 */
public final class CommonEventsEvents {
    private CommonEventsEvents() {}

    /**
     * The name of the main event bus.
     * <p>
     * This can be used in {@link com.kneelawk.commonevents.api.BusEvent}. Using this constant will not cause unwanted
     * class-loading, because the string will get baked into the annotation at compile time.
     */
    public static final String MAIN_EVENT_BUS_NAME = "common_events_events:main";

    /**
     * The main event bus.
     * <p>
     * If you want a bus that is easily accessible to add your events, this bus is the bus. This bus should not be used
     * for more special-purpose events.
     */
    public static final EventBus MAIN_EVENT_BUS = EventBus.builder(ResourceLocation.parse(MAIN_EVENT_BUS_NAME)).build();
}
