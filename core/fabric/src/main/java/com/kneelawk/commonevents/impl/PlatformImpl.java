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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import com.kneelawk.commonevents.api.adapter.mod.ModFileHolder;
import com.kneelawk.commonevents.impl.mod.ModFileHolderImpl;

public class PlatformImpl extends Platform {
    @Override
    public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(CEConstants.MOD_ID).get().getMetadata().getVersion()
            .getFriendlyString();
    }

    @Override
    public List<? extends ModFileHolder> getModFiles() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            // Development environments are janky because FabricLoader only knows which parts of the classpath have
            // fabric.mod.json files, but development environments often have class files in a separate file tree from
            // resources, meaning that FabricLoader does not know which FMJs are associated with which classes.

            // Collect directories on the classpath, as these will usually contain the development files
            List<Path> classpathDirs = new ArrayList<>();
            String[] classpathEntries = System.getProperty("java.class.path").split(":");
            for (String classpathEntry : classpathEntries) {
                Path classpathPath = Path.of(classpathEntry);
                if (Files.exists(classpathPath) && Files.isDirectory(classpathPath)) {
                    classpathDirs.add(classpathPath);
                }
            }

            List<ModFileHolder> modFiles = new ArrayList<>();
            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                List<Path> modRoots = mod.getRootPaths();
                // there are usually very few directories on the classpath, so List.contains shouldn't be too bad
                if (modRoots.stream().anyMatch(classpathDirs::contains)) {
                    // We just assume that any mod whose roots include a directory on our classpath must be a development mod.
                    // There isn't really any good way to differentiate between multiple mods in directories on the classpath,
                    // so we just assume that every mod with directories on the classpath has all directories on the classpath.
                    modFiles.add(new ModFileHolderImpl(mod, classpathDirs));
                } else {
                    modFiles.add(new ModFileHolderImpl(mod));
                }
            }

            return modFiles;
        } else {
            return FabricLoader.getInstance().getAllMods().stream().map(ModFileHolderImpl::new).toList();
        }
    }
}
