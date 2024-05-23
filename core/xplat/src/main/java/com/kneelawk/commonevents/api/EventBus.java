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

package com.kneelawk.commonevents.api;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.adapter.ListenerKey;

/**
 * A collection of events, that selectively registers objects based on which callbacks they implement.
 * <p>
 * Event buses organize events by the event's callback interface type and qualifier. This is because objects registered
 * to an event bus are generally registered based on the type of callback they implement and which qualifier they are
 * annotated with.
 */
public final class EventBus {
    private final ResourceLocation name;
    private final Map<ListenerKey, Event<?>> events = new Object2ObjectOpenHashMap<>();

    private EventBus(ResourceLocation name) {this.name = name;}

    /**
     * Add an event to this event bus. Any future listeners registered with this event bus will be able to be registered
     * to this event.
     *
     * @param event the event to add to the bus.
     */
    public void addEvent(Event<?> event) {
        events.put(ListenerKey.fromClass(event.getType(), event.getQualifier()), event);
    }
}
