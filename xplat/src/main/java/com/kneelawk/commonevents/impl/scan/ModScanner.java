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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import com.kneelawk.commonevents.impl.CELog;
import com.kneelawk.commonevents.impl.mod.ModFileHolder;

public class ModScanner {
    public static @Nullable ModScanner fromJson(ModFileHolder mod, JsonObject obj) {
        if (obj.has("scan")) {
            boolean scanAll = false;
            List<String> scanOnly = new ArrayList<>();

            JsonElement scan = obj.get("scan");
            if (scan.isJsonPrimitive() && scan.getAsBoolean()) {
                scanAll = true;
            } else if (scan.isJsonArray()) {
                JsonArray array = scan.getAsJsonArray();

                for (int i = 0; i < array.size(); i++) {
                    JsonElement scanElem = array.get(i);

                    // TODO: handle adapters
                    if (scanElem.isJsonPrimitive()) {
                        scanOnly.add(scanElem.getAsString());
                    }
                }
            }

            return new ModScanner(scanAll, scanOnly, mod.getModIds(), mod.getRootPaths());
        }

        return null;
    }

    private final boolean scanAll;
    private final List<String> scanOnly;
    private final List<String> modIds;
    private final List<Path> rootPaths;

    public ModScanner(boolean scanAll, List<String> scanOnly, List<String> modIds, List<Path> rootPaths) {
        this.scanAll = scanAll;
        this.scanOnly = scanOnly;
        this.modIds = modIds;
        this.rootPaths = rootPaths;
    }

    public Map<ListenerKey, List<ListenerHandle>> scan(boolean isClientSide) {
        CELog.LOGGER.debug("[Common Events] Scanning {}...", modIds);
        Map<ListenerKey, List<ListenerHandle>> scanned = new HashMap<>();

        if (scanAll) {
            for (Path root : rootPaths) {
                try (Stream<Path> stream = Files.walk(root)) {
                    for (Iterator<Path> iter = stream.iterator(); iter.hasNext(); ) {
                        Path classPath = iter.next();
                        Path fileName = classPath.getFileName();
                        if (fileName != null && fileName.toString().endsWith(".class")) {
                            ClassScanner.scan(classPath, modIds, isClientSide,
                                handle -> scanned.computeIfAbsent(handle.getKey(), k -> new ArrayList<>()).add(handle));
                        }
                    }
                } catch (IOException e) {
                    CELog.LOGGER.warn("[Common Events] Error scanning classes in mod {}.", modIds, e);
                }
            }
        } else if (!scanOnly.isEmpty()) {
            ClassLoader loader = getClass().getClassLoader();

            for (String classStr : scanOnly) {
                URL classPath = loader.getResource(classStr.replace('.', '/') + ".class");
                if (classPath != null) {
                    ClassScanner.scan(classPath, modIds, isClientSide,
                        handle -> scanned.computeIfAbsent(handle.getKey(), k -> new ArrayList<>()).add(handle));
                } else {
                    CELog.LOGGER.warn("[Common Events] Scan class {} not found in mod {}. Skipping...",
                        classStr, modIds);
                }
            }
        }

        CELog.LOGGER.debug("[Common Events] Scanning {} complete.", modIds);

        return scanned;
    }
}
