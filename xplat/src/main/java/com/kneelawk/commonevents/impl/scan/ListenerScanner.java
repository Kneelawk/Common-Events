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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.impl.CELog;
import com.kneelawk.commonevents.impl.Platform;
import com.kneelawk.commonevents.impl.mod.ModFileHolder;

public class ListenerScanner {
    private static final String EVENTS_JSON_PATH = "common-events.json";

    private static boolean initialized = false;
    private static final Lock initLock = new ReentrantLock();
    private static final Map<ListenerKey, List<ListenerHandle>> scanned = new HashMap<>();

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

        List<ListenerHandle> listeners = scanned.get(ListenerKey.fromClass(callbackClass));
        if (listeners != null) {
            for (ListenerHandle handle : listeners) {
                try {
                    T callback = handle.createCallback(callbackClass);
                    event.register(handle.getPhase(), callback);
                } catch (ClassNotFoundException e) {
                    CELog.LOGGER.error("[Common Events] Error creating callback instance for {}", handle, e);
                }
            }
        }
    }

    private static void initialize() {
        CELog.LOGGER.info("[Common Events] Finding mods to scan...");
        Instant start = Instant.now();

        boolean isClientSide = Platform.getInstance().isPhysicalClient();

        // TODO: collect adapters and supply them to fromJson
        List<ModScanner> toScan = new ArrayList<>();

        for (ModFileHolder mod : Platform.getInstance().getModFiles()) {
            Path eventsJson = mod.getResource(EVENTS_JSON_PATH);
            if (eventsJson != null && Files.exists(eventsJson)) {
                try (BufferedReader reader = Files.newBufferedReader(eventsJson)) {
                    JsonElement element = JsonParser.parseReader(reader);
                    if (!element.isJsonObject()) {
                        CELog.LOGGER.warn(
                            "[Common Events] Mod {} common-events.json root is not a JSON object",
                            mod.getModIds());
                        continue;
                    }

                    JsonObject obj = element.getAsJsonObject();

                    ModScanner scanner = ModScanner.fromJson(mod, obj);
                    if (scanner != null) toScan.add(scanner);
                } catch (IOException e) {
                    CELog.LOGGER.warn(
                        "[Common Events] Encountered invalid common-events.json in {}. Skipping...",
                        mod.getModIds(), e);
                }
            }
        }

        CELog.LOGGER.info("[Common Events] Scanning {} mods...", toScan.size());

        for (ModScanner modScanner : toScan) {
            // TODO: consider making this parallel
            var result = modScanner.scan(isClientSide);

            merge(result);
        }

        Instant end = Instant.now();
        Duration loadDuration = Duration.between(start, end);
        CELog.LOGGER.info("[Common Events] Scanned {} mods in {}s, {}ms.", toScan.size(),
            loadDuration.toSeconds(), loadDuration.toMillisPart());
    }

    private static void merge(Map<ListenerKey, List<ListenerHandle>> result) {
        for (Map.Entry<ListenerKey, List<ListenerHandle>> entry : result.entrySet()) {
            List<ListenerHandle> handles = entry.getValue();
            if (!handles.isEmpty()) {
                scanned.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(handles);
            }
        }
    }
}
