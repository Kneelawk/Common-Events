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

import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import org.jetbrains.annotations.Nullable;

public class ModFileHolderImpl implements ModFileHolder {
    private final IModFile mod;

    public ModFileHolderImpl(IModFile mod) {this.mod = mod;}

    @Override
    public List<String> getModIds() {
        return mod.getModInfos().stream().map(IModInfo::getModId).toList();
    }

    @Override
    public @Nullable Path getResource(String path) {
        return mod.findResource(path);
    }

    @Override
    public List<Path> getRootPaths() {
        return List.of(mod.getSecureJar().getRootPath());
    }
}
