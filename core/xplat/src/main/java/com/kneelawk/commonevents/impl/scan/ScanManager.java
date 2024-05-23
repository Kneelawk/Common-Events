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
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.Nullable;

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.api.adapter.ListenerHandle;
import com.kneelawk.commonevents.api.EventKey;
import com.kneelawk.commonevents.api.adapter.mod.ModFileHolder;
import com.kneelawk.commonevents.api.adapter.scan.ScanResult;
import com.kneelawk.commonevents.impl.CELog;
import com.kneelawk.commonevents.impl.Platform;

public class ScanManager {
    private static final String EVENTS_JSON_PATH = "common-events.json";

    private static boolean initialized = false;
    private static final Lock initLock = new ReentrantLock();
    private static final Map<EventKey, List<ListenerHandle>> scanned = new HashMap<>();

    public static final ExecutorService SCAN_EXECUTOR =
        new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors(), 2, TimeUnit.SECONDS,
            new SynchronousQueue<>());
    public static final int THREAD_CUTOFF = 5;

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

    @SuppressWarnings("unchecked")
    public static <T> void addScannedListeners(Event<T> event) {
        ensureInitialized();

        Class<? super T> type = event.getType();

        // We can only instantiate interfaces
        if (!type.isInterface()) return;
        // We can't specify event's based on type parameters
        if (type.getTypeParameters().length > 0) return;

        Method singularMethod = getSingularMethod(type);
        // The type does not have a singular method
        if (singularMethod == null) return;

        String singularMethodName = singularMethod.getName();
        MethodType singularMethodType =
            MethodType.methodType(singularMethod.getReturnType(), singularMethod.getParameterTypes());

        List<ListenerHandle> listeners = scanned.get(event.getKey());
        if (listeners != null) {
            for (ListenerHandle handle : listeners) {
                try {
                    Object callback = handle.createCallback(type, singularMethodName, singularMethodType);
                    ((Event<Object>) event).register(handle.getPhase(), callback);
                } catch (Exception e) {
                    CELog.LOGGER.error("[Common Events] Error creating callback instance for {}", handle, e);
                } catch (Throwable e) {
                    throw new Error(e);
                }
            }
        }
    }

    private static void initialize() {
        CELog.LOGGER.info("[Common Events] Finding mods to scan...");
        Instant start = Instant.now();

        boolean isClientSide = Platform.getInstance().isPhysicalClient();

        List<ModScanner> toScan = new ArrayList<>();

        for (ModFileHolder mod : Platform.getInstance().getModFiles()) {
            Path eventsJson = mod.getResource(EVENTS_JSON_PATH);
            if (eventsJson != null && Files.exists(eventsJson)) {
                try (BufferedReader reader = Files.newBufferedReader(eventsJson)) {
                    JsonElement element = JsonParser.parseReader(reader);
                    if (!element.isJsonObject()) {
                        CELog.LOGGER.warn(
                            "[Common Events] Mod {} common-events.json root is not a JSON object",
                            mod.getModIdStr());
                        continue;
                    }

                    JsonObject obj = element.getAsJsonObject();

                    ModScanner scanner = ModScanner.fromJson(mod, obj);
                    if (scanner != null) toScan.add(scanner);
                } catch (IOException e) {
                    CELog.LOGGER.warn(
                        "[Common Events] Encountered invalid common-events.json in {}. Skipping...",
                        mod.getModIdStr(), e);
                }
            }
        }

        CELog.LOGGER.info("[Common Events] Scanning {} mods...", toScan.size());

        if (toScan.size() >= THREAD_CUTOFF) {
            List<ModScan> resultFutures = new ArrayList<>();
            for (ModScanner modScanner : toScan) {
                resultFutures.add(new ModScan(modScanner,
                    CompletableFuture.supplyAsync(() -> modScanner.scan(isClientSide), SCAN_EXECUTOR)));
            }

            for (ModScan scan : resultFutures) {
                try {
                    ScanResult result = scan.future().get();

                    merge(result.listeners());
                } catch (InterruptedException | ExecutionException e) {
                    CELog.LOGGER.warn("[Common Events] Encountered error while scanning {}", scan.scanner().getModIds(),
                        e);
                }
            }
        } else {
            for (ModScanner modScanner : toScan) {
                ScanResult result = modScanner.scan(isClientSide);

                merge(result.listeners());
            }
        }

        Instant end = Instant.now();
        Duration loadDuration = Duration.between(start, end);
        CELog.LOGGER.info("[Common Events] Scanned {} mods in {}s, {}ms.", toScan.size(),
            loadDuration.toSeconds(), loadDuration.toMillisPart());
    }

    private static void merge(Map<EventKey, List<ListenerHandle>> result) {
        for (Map.Entry<EventKey, List<ListenerHandle>> entry : result.entrySet()) {
            List<ListenerHandle> handles = entry.getValue();
            if (!handles.isEmpty()) {
                scanned.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(handles);
            }
        }
    }

    /**
     * Gets the singular abstract method in a functional interface.
     *
     * @param interfaceClass the interface to find the singular abstract method of.
     * @return the interface's singular abstract method.
     */
    public static @Nullable Method getSingularMethod(Class<?> interfaceClass) {
        Method singularMethod = null;

        for (Method method : interfaceClass.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                if (singularMethod != null) return null;
                singularMethod = method;
            }
        }

        return singularMethod;
    }

    private record ModScan(ModScanner scanner, CompletableFuture<ScanResult> future) {}
}
