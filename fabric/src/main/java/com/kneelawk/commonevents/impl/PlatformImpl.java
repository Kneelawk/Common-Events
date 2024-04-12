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

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import com.kneelawk.commonevents.impl.mod.ModFileHolder;
import com.kneelawk.commonevents.impl.mod.ModFileHolderImpl;

public class PlatformImpl extends Platform {
    @Override
    public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(CommonEventsImpl.MOD_ID).get().getMetadata().getVersion()
            .getFriendlyString();
    }

    @Override
    public List<? extends ModFileHolder> getModFiles() {
        return FabricLoader.getInstance().getAllMods().stream().map(ModFileHolderImpl::new).toList();
    }
}
