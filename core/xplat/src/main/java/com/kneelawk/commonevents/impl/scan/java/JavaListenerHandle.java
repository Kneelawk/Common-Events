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

package com.kneelawk.commonevents.impl.scan.java;

import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import net.minecraft.resources.Identifier;

import com.kneelawk.commonevents.api.EventKey;
import com.kneelawk.commonevents.api.adapter.CallbackSettings;
import com.kneelawk.commonevents.api.adapter.ListenerHandle;
import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;
import com.kneelawk.commonevents.api.adapter.util.ListenerBuilder;

public class JavaListenerHandle implements ListenerHandle {
    private final EventKey key;
    private final Identifier phase;
    private final Type listenerClass;
    private final String methodName;
    private final Type methodDescriptor;

    public JavaListenerHandle(EventKey key, Identifier phase, Type listenerClass, String methodName,
                              Type methodDescriptor) {
        this.key = key;
        this.phase = phase;
        this.listenerClass = listenerClass;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public @NotNull EventKey getKey() {
        return key;
    }

    @Override
    public @NotNull Identifier getPhase() {
        return phase;
    }

    @Override
    public <T> @NotNull T createCallback(@NotNull CallbackSettings<T> settings)
        throws Throwable {
        Class<?> listenerClazz = Class.forName(listenerClass.getClassName());
        Method listenerMethod = AdapterUtils.getDeclaredMethod(listenerClazz, methodName, methodDescriptor);
        return ListenerBuilder.buildStaticListener(settings.interfaceClass(), listenerClazz, listenerMethod,
            settings.builderSettings());
    }

    @Override
    public String toString() {
        return "JavaListenerHandle{" + key + "(" + phase + ") -> " +
            listenerClass.getInternalName() + "." + methodName + methodDescriptor + '}';
    }
}
