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

import org.objectweb.asm.Type;

import net.minecraft.resources.ResourceLocation;

public class SimpleListenerHandle implements ListenerHandle {
    private static final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    private final ListenerKey key;
    private final ResourceLocation phase;
    private final Type listenerClass;
    private final String methodName;
    private final Type methodDescriptor;

    public SimpleListenerHandle(ListenerKey key, ResourceLocation phase, Type listenerClass, String methodName,
                                Type methodDescriptor) {
        this.key = key;
        this.phase = phase;
        this.listenerClass = listenerClass;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public ListenerKey getKey() {
        return key;
    }

    @Override
    public ResourceLocation getPhase() {
        return phase;
    }

    @Override
    public <T> T createCallback(Class<T> callbackClass) throws ClassNotFoundException {
        Class<?> listenerClazz = Class.forName(listenerClass.getClassName());

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String toString() {
        return "SimpleListenerHandle{" +
            "key=" + key +
            ", phase=" + phase +
            ", class='" + listenerClass + '\'' +
            ", method='" + methodName + methodDescriptor + '\'' +
            '}';
    }
}
