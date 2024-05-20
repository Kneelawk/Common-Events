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

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

/**
 * Describes how a mod wishes to be scanned.
 */
@ApiStatus.NonExtendable
public sealed interface ScannableInfo {
    /**
     * Describes that the mod wishes to have all its classes scanned.
     */
    final class All implements ScannableInfo {
        private All() {}

        /**
         * Singleton instance.
         */
        public static final All INSTANCE = new All();
    }

    /**
     * Describes that the mod wishes to only have the given classes scanned.
     *
     * @param classes the classes to scan.
     */
    record Only(List<String> classes) implements ScannableInfo {
    }
}
