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

package com.kneelawk.commonevents.impl.scan;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.impl.CommonEventsImpl;
import com.kneelawk.commonevents.impl.Platform;
import com.kneelawk.commonevents.impl.mod.ModFileHolder;

public class ListenerScanner {
    private static boolean initialized = false;
    private static final Lock initLock = new ReentrantLock();
    private static final Map<ListenerKey, List<ListenerHandle>> handles = new HashMap<>();

    public static void ensureInitialized() {
        initLock.lock();
        try {
            if (!initialized) {
                initialized = true;
                initialize();
            }
        } finally {
            initLock.unlock();
        }
    }

    public static <T> void addScannedListeners(Class<T> callbackClass, Event<T> event) {
        ensureInitialized();

        List<ListenerHandle> listeners = handles.get(ListenerKey.fromClass(callbackClass));
        if (listeners != null) {
            for (ListenerHandle handle : listeners) {
                event.register(handle.getPhase(), handle.createCallback(callbackClass));
            }
        }
    }

    private static void initialize() {
        CommonEventsImpl.LOGGER.info("[Common Events] Scanning mods...");
        Instant start = Instant.now();
        for (ModFileHolder mod : Platform.getInstance().getModFiles()) {
            CommonEventsImpl.LOGGER.info("[Common Events] Scanning mod {}...", mod.getModIds());
            CommonEventsImpl.LOGGER.info("[Common Events] Root paths: {}", mod.getRootPaths());
        }
        Instant end = Instant.now();
        Duration loadDuration = Duration.between(start, end);
        CommonEventsImpl.LOGGER.info("[Common Events] Scanning finished in {}s, {}ms.", loadDuration.toSeconds(),
            loadDuration.toMillisPart());
    }
}
