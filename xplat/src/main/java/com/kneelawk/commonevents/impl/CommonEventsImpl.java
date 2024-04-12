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

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.impl.scan.ListenerScanner;

public class CommonEventsImpl {
    public static final String MOD_ID = "common_events";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static void init() {
        LOGGER.info("Initializing Common Events {}...", Platform.getInstance().getModVersion());
        ListenerScanner.ensureInitialized();
    }

    /// Following code copied from QSL ///

    /*
     * Copyright 2016, 2017, 2018, 2019 FabricMC
     * Copyright 2021 The Quilt Project
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

    public static void ensureContainsDefaultPhase(ResourceLocation[] defaultPhases) {
        for (var id : defaultPhases) {
            if (id.equals(Event.DEFAULT_PHASE)) {
                return;
            }
        }

        throw new IllegalArgumentException("The event phases must contain Event.DEFAULT_PHASE.");
    }

    /**
     * Ensures that the given array does not contain duplicates, otherwise throw an exception.
     *
     * @param items            the array of items to check
     * @param exceptionFactory the exception factory in the case of a duplicate
     * @param <T>              the type of items of the array
     */
    public static <T> void ensureNoDuplicates(T[] items, Function<T, IllegalArgumentException> exceptionFactory) {
        for (int i = 0; i < items.length; ++i) {
            for (int j = i + 1; j < items.length; ++j) {
                if (items[i].equals(items[j])) {
                    throw exceptionFactory.apply(items[i]);
                }
            }
        }
    }
}
