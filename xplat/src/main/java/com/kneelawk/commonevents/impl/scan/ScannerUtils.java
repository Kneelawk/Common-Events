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

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

public class ScannerUtils {
    private ScannerUtils() {}

    public static MethodType getMethodType(Type descriptor) throws ClassNotFoundException {
        Class<?> returnClass = getClass(descriptor.getReturnType());
        Type[] argTypes = descriptor.getArgumentTypes();
        Class<?>[] argClasses = new Class[argTypes.length];

        for (int i = 0; i < argTypes.length; i++) {
            argClasses[i] = getClass(argTypes[i]);
        }

        return MethodType.methodType(returnClass, argClasses);
    }

    public static Class<?> getClass(Type type) throws ClassNotFoundException {
        return switch (type.getSort()) {
            case Type.VOID -> Void.TYPE;
            case Type.BOOLEAN -> Boolean.TYPE;
            case Type.CHAR -> Character.TYPE;
            case Type.BYTE -> Byte.TYPE;
            case Type.SHORT -> Short.TYPE;
            case Type.INT -> Integer.TYPE;
            case Type.FLOAT -> Float.TYPE;
            case Type.LONG -> Long.TYPE;
            case Type.DOUBLE -> Double.TYPE;
            case Type.ARRAY -> {
                Class<?> clazz = getClass(type.getElementType());
                int dimensions = type.getDimensions();
                for (int i = 0; i < dimensions; i++) clazz = clazz.arrayType();
                yield clazz;
            }
            case Type.OBJECT -> Class.forName(type.getClassName());
            default -> throw new AssertionError("Unexpected value: " + type.getSort());
        };
    }

    public static @Nullable Method getSingularMethod(Class<?> interfaceClass) {
        Method singularMethod = null;

        for (Method method : interfaceClass.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                if (singularMethod != null) return null;
                singularMethod = method;
            }
        }

        return singularMethod;
    }
}
