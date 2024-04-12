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

import java.lang.invoke.MethodHandles;

import net.minecraft.resources.ResourceLocation;

public class ListenerHandle {
    private static final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    private final ListenerKey key;
    private final ResourceLocation phase;
    private final String listenerClass;
    private final String methodName;
    private final String methodDescriptor;

    public ListenerHandle(ListenerKey key, ResourceLocation phase, String listenerClass, String methodName,
                          String methodDescriptor) {
        this.key = key;
        this.phase = phase;
        this.listenerClass = listenerClass;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
    }

    public ListenerKey getKey() {
        return key;
    }

    public ResourceLocation getPhase() {
        return phase;
    }

    public <T> T createCallback(Class<T> callbackClass) throws ClassNotFoundException {
        Class<?> listenerClazz = Class.forName(listenerClass);

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
