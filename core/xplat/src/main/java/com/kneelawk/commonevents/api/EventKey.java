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

package com.kneelawk.commonevents.api;

import org.objectweb.asm.Type;

import com.kneelawk.commonevents.api.adapter.ListenerHandle;
import com.kneelawk.commonevents.impl.CEConstants;

/**
 * A unique key that describes an {@link Event}.
 * <p>
 * When scanning this is used to key {@link ListenerHandle}s to determine which events they are for.
 *
 * @param type      the callback interface type the listener implements.
 * @param qualifier the listener's additional qualifier or {@link Event#DEFAULT_QUALIFIER} if none.
 */
public record EventKey(Type type, String qualifier) {
    /**
     * Creates an event key from a class instead of an ASM type.
     *
     * @param clazz     the class of the callback interface.
     * @param qualifier the additional qualifier.
     * @return the newly created event key.
     */
    public static EventKey fromClass(Class<?> clazz, String qualifier) {
        return new EventKey(Type.getType(clazz), qualifier);
    }

    @Override
    public String toString() {
        if (CEConstants.DEFAULT_QUALIFIER.equals(qualifier)) return type.getInternalName();
        return "(" + type.getInternalName() + "|" + qualifier + ")";
    }
}
