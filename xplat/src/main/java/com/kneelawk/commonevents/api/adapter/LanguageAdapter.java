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

import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import com.kneelawk.commonevents.api.adapter.scan.ScanRequest;
import com.kneelawk.commonevents.api.adapter.scan.ScanResult;
import com.kneelawk.commonevents.impl.CELog;

/**
 * Interface implemented by all language adapters.
 */
public interface LanguageAdapter {
    /**
     * The id of the official java adapter.
     */
    String JAVA_ADAPTER_ID = "java";

    /**
     * The id of the official kotlin adapter.
     */
    String KOTLIN_ADAPTER_ID = "kotlin";

    /**
     * The id of the default adapter used if none are specified.
     */
    String DEFAULT_ADAPTER_ID = JAVA_ADAPTER_ID;

    /**
     * The list of loaded language adapters.
     */
    Map<String, LanguageAdapter> ADAPTERS = loadAdapters();

    private static Map<String, LanguageAdapter> loadAdapters() {
        Map<String, LanguageAdapter> adapters =
            ServiceLoader.load(LanguageAdapter.class).stream().map(ServiceLoader.Provider::get)
                .collect(ImmutableMap.toImmutableMap(LanguageAdapter::getId, Function.identity()));
        if (adapters.isEmpty()) throw new IllegalStateException("[Common Events] No language adapters found");

        CELog.LOGGER.info("[Common Events] Loaded language adapters: {}", adapters.keySet());

        return adapters;
    }

    /**
     * {@return the default language adapter}
     */
    static LanguageAdapter getDefault() {
        LanguageAdapter def = ADAPTERS.get(DEFAULT_ADAPTER_ID);
        if (def == null)
            throw new IllegalStateException("[Common Events] Default adapter '" + DEFAULT_ADAPTER_ID + "' not present");
        return def;
    }

    /**
     * {@return this language adapter's id}
     */
    String getId();

    /**
     * Requests that this language adapter scans the given mod.
     * <p>
     * This may be executed several times on different thread in parallel.
     * Scanners should avoid unneeded synchronization.
     *
     * @param request the description of what to scan and how.
     * @return the result of the scan.
     */
    ScanResult scan(ScanRequest request);
}
