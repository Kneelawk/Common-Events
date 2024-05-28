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

package com.kneelawk.commonevents.api.adapter;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.EventBus;

/**
 * Holds a reference to an event field that can be added to an event bus when that bus is initialized.
 */
public interface BusEventHandle {
    /**
     * Gets the names of all the buses this handle's event wishes to be added to.
     *
     * @return all buses this event should be added to.
     */
    ResourceLocation[] getBusNames();

    /**
     * Adds the referenced event to the given bus.
     *
     * @param addTo the bus to add this handle's event to.
     * @throws Throwable if an error occurs while obtaining the event instance or adding it to the bus. If an exception
     *                   is thrown, the event bus should not contain this handler's event.
     */
    void addToBus(EventBus addTo) throws Throwable;
}
