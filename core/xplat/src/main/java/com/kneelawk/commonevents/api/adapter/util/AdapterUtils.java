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

package com.kneelawk.commonevents.api.adapter.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

import org.objectweb.asm.Type;

/**
 * Utilities for language adapters.
 */
public final class AdapterUtils {
    /**
     * The recommended method handle lookup for all adapters.
     */
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private AdapterUtils() {}

    /**
     * Gets the {@link MethodType} described by a method {@link Type}.
     * <p>
     * Note: this does load all mentioned classes.
     *
     * @param descriptor the method type.
     * @return the equivalent method type.
     * @throws ClassNotFoundException if the given type mentions classes that do not exist.
     */
    public static MethodType getMethodType(Type descriptor) throws ClassNotFoundException {
        Class<?> returnClass = getClass(descriptor.getReturnType());
        Type[] argTypes = descriptor.getArgumentTypes();
        Class<?>[] argClasses = new Class[argTypes.length];

        for (int i = 0; i < argTypes.length; i++) {
            argClasses[i] = getClass(argTypes[i]);
        }

        return MethodType.methodType(returnClass, argClasses);
    }

    /**
     * Converts a {@link MethodType} into a method {@link Type}.
     *
     * @param methodType the method type.
     * @return the equivalent method type.
     */
    public static Type getMethodType(MethodType methodType) {
        return Type.getMethodType(Type.getType(methodType.returnType()),
            Arrays.stream(methodType.parameterArray()).map(Type::getType).toArray(Type[]::new));
    }

    /**
     * Gets the class described by a {@link Type}.
     * <p>
     * Note: this does load the associated class.
     *
     * @param type the type to get the class of.
     * @return the loaded class of the given type.
     * @throws ClassNotFoundException if the given type describes a class that does not exist.
     */
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
}