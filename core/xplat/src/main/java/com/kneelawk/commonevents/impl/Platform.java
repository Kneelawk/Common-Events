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

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;

import com.kneelawk.commonevents.api.adapter.mod.ModFileHolder;

public abstract class Platform {
    private static final Platform INSTANCE;

    static {
        try {
            INSTANCE =
                (Platform) Class.forName("com.kneelawk.commonevents.impl.PlatformImpl").getConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Platform getInstance() {
        return INSTANCE;
    }
    
    public abstract boolean isPhysicalClient();
    
    public abstract String getModVersion();
    
    public abstract List<? extends ModFileHolder> getModFiles();
    
    public abstract Path getGameDirectory();
}
