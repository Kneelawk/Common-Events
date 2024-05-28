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

import java.lang.reflect.Field;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.commonevents.api.adapter.BusEventHandle;
import com.kneelawk.commonevents.api.adapter.scan.BadEventException;

public class JavaBusEventHandle implements BusEventHandle {
    private final ResourceLocation[] busNames;
    private final Type holderClass;
    private final String fieldName;

    public JavaBusEventHandle(ResourceLocation[] busNames, Type holderClass, String fieldName) {
        this.busNames = busNames;
        this.holderClass = holderClass;
        this.fieldName = fieldName;
    }

    @Override
    public ResourceLocation @NotNull [] getBusNames() {
        return busNames;
    }

    @Override
    public Event<?> getEvent() throws Throwable {
        Class<?> holderClazz = Class.forName(holderClass.getClassName());
        Field field = holderClazz.getDeclaredField(fieldName);

        Event<?> event = (Event<?>) field.get(null);
        if (event == null) {
            throw new BadEventException(
                "Encountered @BusEvent annotated field that has not been statically initialized");
        }

        return event;
    }

    @Override
    public String toString() {
        return "JavaBusEventHandle{" + holderClass.getInternalName() + "." + fieldName + " -> " +
            Arrays.toString(busNames) + "}";
    }
}
