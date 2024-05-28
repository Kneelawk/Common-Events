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

package com.kneelawk.commonevents.api.adapter.scan;

import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.EventKey;
import com.kneelawk.commonevents.api.adapter.BusEventHandle;
import com.kneelawk.commonevents.api.adapter.ListenerHandle;

/**
 * The result of scanning a mod.
 *
 * @param listeners the listeners found while scanning a mod.
 * @param events    the bus events found while scanning a mod.
 */
public record ScanResult(Map<EventKey, List<ListenerHandle>> listeners,
                         Map<ResourceLocation, List<BusEventHandle>> events) {
}
