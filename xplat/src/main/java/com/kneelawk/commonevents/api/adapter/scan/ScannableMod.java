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

package com.kneelawk.commonevents.api.adapter.scan;

import org.jetbrains.annotations.ApiStatus;

import com.kneelawk.commonevents.api.adapter.mod.ModFileHolder;

/**
 * Describes a mod that can be scanned.
 */
@ApiStatus.NonExtendable
public interface ScannableMod {
    /**
     * {@return the mod file associated with this scannable mod}
     */
    ModFileHolder getModFile();

    /**
     * {@return the info on how the mod wishes to be scanned}
     */
    ScannableInfo getInfo();
}
