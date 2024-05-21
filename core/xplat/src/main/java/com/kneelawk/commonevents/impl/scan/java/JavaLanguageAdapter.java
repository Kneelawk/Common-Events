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

package com.kneelawk.commonevents.impl.scan.java;

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

import org.jetbrains.annotations.NotNull;

import com.kneelawk.commonevents.api.adapter.LanguageAdapter;
import com.kneelawk.commonevents.api.adapter.ListenerHandle;
import com.kneelawk.commonevents.api.adapter.ListenerKey;
import com.kneelawk.commonevents.api.adapter.mod.ModFileHolder;
import com.kneelawk.commonevents.api.adapter.scan.ScanRequest;
import com.kneelawk.commonevents.api.adapter.scan.ScanResult;
import com.kneelawk.commonevents.api.adapter.scan.ScannableInfo;
import com.kneelawk.commonevents.api.adapter.scan.ScannableMod;
import com.kneelawk.commonevents.impl.CELog;

public class JavaLanguageAdapter implements LanguageAdapter {
    @Override
    public @NotNull String getId() {
        return JAVA_ADAPTER_ID;
    }

    @Override
    public @NotNull ScanResult scan(@NotNull ScanRequest request) {
        ScannableMod mod = request.getMod();
        ModFileHolder modFile = mod.getModFile();
        String modIds = modFile.getModIdStr();

        Map<ListenerKey, List<ListenerHandle>> scanned = new HashMap<>();
        ClassLoader loader = getClass().getClassLoader();

        if (mod.getInfo() instanceof ScannableInfo.All) {
            for (Path root : modFile.getRootPaths()) {
                try (Stream<Path> stream = Files.walk(root)) {
                    for (Iterator<Path> iter = stream.iterator(); iter.hasNext(); ) {
                        Path classPath = iter.next();
                        Path fileName = classPath.getFileName();
                        if (fileName != null && fileName.toString().endsWith(".class")) {
                            ClassScanner.scan(classPath, modIds, request.isClientSide(), false,
                                handle -> scanned.computeIfAbsent(handle.getKey(), k -> new ArrayList<>()).add(handle));
                        }
                    }
                } catch (Exception e) {
                    CELog.LOGGER.warn("[Common Events] Error scanning classes in mod {}.", modIds, e);
                }
            }
        } else if (mod.getInfo() instanceof ScannableInfo.Only only && !only.classes().isEmpty()) {
            for (String classStr : only.classes()) {
                URL classPath = loader.getResource(classStr.replace('.', '/') + ".class");
                if (classPath != null) {
                    ClassScanner.scan(classPath, modIds, request.isClientSide(), true,
                        handle -> scanned.computeIfAbsent(handle.getKey(), k -> new ArrayList<>()).add(handle));
                } else {
                    CELog.LOGGER.warn("[Common Events] Scan class {} not found in mod {}. Skipping...",
                        classStr, modIds);
                }
            }
        }

        return new ScanResult(scanned);
    }
}
