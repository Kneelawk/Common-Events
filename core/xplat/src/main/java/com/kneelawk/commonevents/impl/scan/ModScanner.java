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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import com.kneelawk.commonevents.api.adapter.LanguageAdapter;
import com.kneelawk.commonevents.api.adapter.mod.ModFileHolder;
import com.kneelawk.commonevents.api.adapter.scan.ScanRequest;
import com.kneelawk.commonevents.api.adapter.scan.ScanResult;
import com.kneelawk.commonevents.api.adapter.scan.ScannableInfo;
import com.kneelawk.commonevents.api.adapter.scan.ScannableMod;
import com.kneelawk.commonevents.impl.CELog;

public class ModScanner {
    public static @Nullable ModScanner fromJson(ModFileHolder mod, JsonObject obj) {
        LanguageAdapter adapter = LanguageAdapter.getDefault();
        if (obj.has("adapter")) {
            JsonElement adapterElem = obj.get("adapter");
            if (adapterElem.isJsonPrimitive()) {
                String adapterId = adapterElem.getAsString();
                if (LanguageAdapter.ADAPTERS.containsKey(adapterId)) {
                    adapter = LanguageAdapter.ADAPTERS.get(adapterId);
                } else {
                    CELog.LOGGER.warn("[Common Events] Unknown adapter: '{}'", adapterId);
                }
            }
        }

        if (obj.has("scan")) {
            List<String> scanOnly = new ArrayList<>();

            ScannableInfo info = new ScannableInfo.Only(scanOnly);
            JsonElement scan = obj.get("scan");
            if (scan.isJsonPrimitive() && scan.getAsBoolean()) {
                info = ScannableInfo.All.INSTANCE;
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

            return new ModScanner(new ScannableModImpl(mod, info), adapter);
        }

        return null;
    }

    private final ScannableMod mod;
    private final LanguageAdapter adapter;

    private ModScanner(ScannableMod mod, LanguageAdapter adapter) {
        this.mod = mod;
        this.adapter = adapter;
    }

    public List<String> getModIds() {
        return mod.getModFile().getModIds();
    }

    public ScanResult scan(boolean isClientSide) {
        List<String> modIds = mod.getModFile().getModIds();
        CELog.LOGGER.debug("[Common Events] Scanning {} with adapter '{}'...", modIds, adapter.getId());

        ScanResult result = adapter.scan(new ScanRequestImpl(mod, isClientSide));

        CELog.LOGGER.debug("[Common Events] Scanning {} with adapter '{}' complete.", modIds, adapter.getId());
        return result;
    }

    private record ScannableModImpl(ModFileHolder modFile, ScannableInfo info) implements ScannableMod {
        @Override
        public ModFileHolder getModFile() {
            return modFile;
        }

        @Override
        public ScannableInfo getInfo() {
            return info;
        }
    }

    private record ScanRequestImpl(ScannableMod mod, boolean clientSide) implements ScanRequest {
        @Override
        public ScannableMod getMod() {
            return mod;
        }

        @Override
        public boolean isClientSide() {
            return clientSide;
        }
    }
}
