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

package com.kneelawk.commonevents.api.adapter.mod;

import java.nio.file.Path;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A platform-independent representation of a mod file.
 */
@ApiStatus.NonExtendable
public interface ModFileHolder {
    /**
     * {@return a list of mod ids associated with this mod file}
     */
    List<String> getModIds();

    /**
     * Gets a resource at the given location within this mod.
     * <p>
     * Resources may or may not include class files.
     *
     * @param path the resource path.
     * @return the path with access to the resource, if it exists.
     */
    @Nullable
    Path getResource(String path);

    /**
     * Gets all this mod's root paths.
     * <p>
     * A mod may have multiple root paths, for example, if it is spread over a set of directories.
     *
     * @return this mod's root paths.
     */
    List<Path> getRootPaths();
}
