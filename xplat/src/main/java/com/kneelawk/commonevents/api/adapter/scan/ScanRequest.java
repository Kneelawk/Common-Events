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

/**
 * Describes a request to scan a specific mod.
 */
@ApiStatus.NonExtendable
public interface ScanRequest {
    /**
     * {@return the mod to be scanned}
     */
    ScannableMod getMod();

    /**
     * {@return whether the scan is taking place on the physical client or the physical server}
     */
    boolean isClientSide();
}
