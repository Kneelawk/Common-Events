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

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.adapter.ListenerHandle;
import com.kneelawk.commonevents.api.EventKey;
import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;
import com.kneelawk.commonevents.impl.CELog;

public class JavaListenerHandle implements ListenerHandle {
    private final EventKey key;
    private final ResourceLocation phase;
    private final Type listenerClass;
    private final String methodName;
    private final Type methodDescriptor;

    public JavaListenerHandle(EventKey key, ResourceLocation phase, Type listenerClass, String methodName,
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
    public @NotNull ResourceLocation getPhase() {
        return phase;
    }

    @Override
    public <T> @NotNull T createCallback(@NotNull Class<T> callbackClass, @NotNull String singularMethodName,
                                         @NotNull MethodType singularMethodType)
        throws Throwable {
        Class<?> listenerClazz = Class.forName(listenerClass.getClassName());
        MethodType methodType = AdapterUtils.getMethodType(methodDescriptor);

        if (!singularMethodType.returnType().isAssignableFrom(methodType.returnType())) {
            Type singularType = AdapterUtils.getMethodType(singularMethodType);
            CELog.LOGGER.warn(
                "[Common Events] Callback listener {}.{}{} has return type that is incompatible with callback interface {}.{}{}. " +
                    "The associated event may throw a ClassCastException when called.",
                listenerClass.getInternalName(), methodName, methodDescriptor,
                callbackClass.getName().replace('.', '/'), singularMethodName, singularType);
        }

        MethodHandle handle = AdapterUtils.LOOKUP.findStatic(listenerClazz, methodName, methodType);

        return callbackClass.cast(LambdaMetafactory.metafactory(AdapterUtils.LOOKUP, singularMethodName,
                MethodType.methodType(callbackClass), singularMethodType, handle, singularMethodType).getTarget()
            .invoke());
    }

    @Override
    public String toString() {
        return "JavaListenerHandle{" + key + "(" + phase + ") -> " +
            listenerClass.getInternalName() + "." + methodName + methodDescriptor + '}';
    }
}
