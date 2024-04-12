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

package com.kneelawk.commonevents.impl.mod;

import java.nio.file.Path;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loader.api.ModContainer;

public class ModFileHolderImpl implements ModFileHolder {
    private final ModContainer mod;
    private final List<Path> alternateRoots;

    public ModFileHolderImpl(ModContainer mod) {
        this(mod, List.of());
    }

    public ModFileHolderImpl(ModContainer mod, List<Path> alternateRoots) {
        this.mod = mod;
        this.alternateRoots = alternateRoots;
    }

    @Override
    public List<String> getModIds() {
        return List.of(mod.getMetadata().getId());
    }

    @Override
    public @Nullable Path getResource(String path) {
        return mod.findPath(path).orElse(null);
    }

    @Override
    public List<Path> getRootPaths() {
        if (alternateRoots.isEmpty()) {
            return mod.getRootPaths();
        } else {
            return alternateRoots;
        }
    }
}
