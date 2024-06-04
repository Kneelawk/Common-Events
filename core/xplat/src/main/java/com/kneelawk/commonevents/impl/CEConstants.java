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

package com.kneelawk.commonevents.impl;

import net.minecraft.resources.ResourceLocation;

public class CEConstants {
    public static final String MOD_ID = "common_events";

    public static final ResourceLocation DEFAULT_PHASE = rl("default");

    public static final String DEFAULT_QUALIFIER = "common_events_default";
    
    public static final int SCAN_MULTI_THREAD_THRESHOLD = Integer.getInteger("com.kneelawk.common_events.scan_multi_thread_threshold", 5);
    public static final boolean EXPORT_GENERATED_CLASSES = Boolean.getBoolean("com.kneelawk.common_events.export_generated_classes");

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
