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

package com.kneelawk.commonevents.impl.gen;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;
import com.kneelawk.commonevents.impl.CEConstants;
import com.kneelawk.commonevents.impl.CELog;
import com.kneelawk.commonevents.impl.Platform;

public class ImplementationGenerator {
    private static final String PREFIX = "com.kneelawk.commonevents.impl.gen.impl.$CommonEvents_Generated$.";
    private static final Handle LMF_HANDLE =
        new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", //
            "(" + //
                "Ljava/lang/invoke/MethodHandles$Lookup;" + //
                "Ljava/lang/String;Ljava/lang/invoke/MethodType;" + //
                "Ljava/lang/invoke/MethodType;" + //
                "Ljava/lang/invoke/MethodHandle;" + //
                "Ljava/lang/invoke/MethodType;" + //
                ")" + //
                "Ljava/lang/invoke/CallSite;", //
            false);
    private static final Loader LOADER =
        new Loader("event-implementation-generator", ImplementationGenerator.class.getClassLoader());

    private static class Loader extends ClassLoader {
        public Loader(String name, ClassLoader parent) {
            super(name, parent);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (!name.startsWith(PREFIX)) throw new ClassNotFoundException(name);

            String interfaceName = name.substring(PREFIX.length());
            Class<?> interfaceClass = Class.forName(interfaceName);

            String internalName = name.replace('.', '/');
            byte[] bytes = generateClass(Type.getObjectType(internalName), interfaceClass);

            if (CEConstants.EXPORT_GENERATED_CLASSES) {
                Path classPath =
                    Platform.getInstance().getGameDirectory().resolve(".common-events/" + internalName + ".class");
                try {
                    Path parentPath = classPath.getParent();
                    if (!Files.exists(parentPath)) {
                        Files.createDirectories(parentPath);
                    }
                    Files.write(classPath, bytes);
                } catch (IOException e) {
                    CELog.LOGGER.warn("[Common Events] Unable to write exported generated class to {}", classPath, e);
                }
            }

            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Function<T[], T> defineSimple(Class<? super T> interfaceClass) {
        if (!interfaceClass.isInterface())
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");

        java.lang.reflect.Method interfaceMethod = AdapterUtils.getSingularMethod(interfaceClass);
        if (interfaceMethod == null)
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");

        if (!Void.TYPE.equals(interfaceMethod.getReturnType())) throw new IllegalArgumentException(
            interfaceClass.getName() +
                " is not a simple functional interface. Simple functional interfaces must not return anything.");

        try {
            return (Function<T[], T>) LOADER.loadClass(PREFIX + interfaceClass.getName()).getConstructor()
                .newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Unable to generate simple implementation for " + interfaceClass.getName(), e);
        }
    }

    private static byte[] generateClass(Type name, Class<?> interfaceClass) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        Type interfaceType = Type.getType(interfaceClass);
        Type interfaceArrayType = Type.getType("[" + interfaceType.getDescriptor());
        java.lang.reflect.Method interfaceMethod = AdapterUtils.getSingularMethod(interfaceClass);
        assert interfaceMethod != null;
        Method interfaceMethodName = Method.getMethod(interfaceMethod);
        Type interfaceMethodType = Type.getType(interfaceMethod);
        Type[] interfaceMethodArgs = interfaceMethodName.getArgumentTypes();

        Type functionType = Type.getType(Function.class);
        Method applyMethodName =
            new Method("apply", interfaceType, new Type[]{interfaceArrayType});

        Type objectType = Type.getType(Object.class);

        String signature = objectType.getDescriptor() + "L" + functionType.getInternalName() + "<" +
            interfaceArrayType.getDescriptor() + interfaceType.getDescriptor() + ">;";

        writer.visit(AdapterUtils.JAVA_VERSION, Opcodes.ACC_PUBLIC, name.getInternalName(), signature,
            objectType.getInternalName(), new String[]{functionType.getInternalName()});
        writer.visitInnerClass("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup",
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC);

        Method initMethod = Method.getMethod("void <init> ()");
        GeneratorAdapter constructor = new GeneratorAdapter(Opcodes.ACC_PUBLIC, initMethod, null, null, writer);
        constructor.loadThis();
        constructor.invokeConstructor(objectType, initMethod);
        constructor.returnValue();
        constructor.endMethod();

        Method lambdaMethodName =
            new Method("lambda$apply$0", Type.VOID_TYPE, prefix(interfaceArrayType, interfaceMethodArgs));

        GeneratorAdapter apply = new GeneratorAdapter(Opcodes.ACC_PUBLIC, applyMethodName, null, null, writer);
        apply.loadArg(0);
        apply.invokeDynamic(interfaceMethodName.getName(), applyMethodName.getDescriptor(), LMF_HANDLE,
            interfaceMethodType, new Handle(Opcodes.H_INVOKESTATIC, name.getInternalName(), lambdaMethodName.getName(),
                lambdaMethodName.getDescriptor(), false), interfaceMethodType);
        apply.returnValue();
        apply.endMethod();

        GeneratorAdapter applyBridge =
            new GeneratorAdapter(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE,
                new Method("apply", objectType, new Type[]{objectType}), null, null, writer);
        applyBridge.loadThis();
        applyBridge.loadArg(0);
        applyBridge.checkCast(interfaceArrayType);
        applyBridge.invokeVirtual(name, applyMethodName);
        applyBridge.returnValue();
        applyBridge.endMethod();

        GeneratorAdapter lambda =
            new GeneratorAdapter(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC, lambdaMethodName,
                null, null, writer);
        lambda.loadArg(0);
        lambda.arrayLength();
        int lenLocal = lambda.newLocal(Type.INT_TYPE);
        lambda.storeLocal(lenLocal);
        lambda.push(0);
        int iLocal = lambda.newLocal(Type.INT_TYPE);
        lambda.storeLocal(iLocal);

        Label loop = lambda.newLabel();
        Label end = lambda.newLabel();
        lambda.visitLabel(loop);
        lambda.loadLocal(iLocal);
        lambda.loadLocal(lenLocal);
        lambda.ifICmp(GeneratorAdapter.GE, end);

        lambda.loadArg(0);
        lambda.loadLocal(iLocal);
        lambda.arrayLoad(interfaceType);
        for (int argIndex = 0; argIndex < interfaceMethodArgs.length; argIndex++) {
            // argIndex + 1 because the first arg is the array of callbacks
            lambda.loadArg(argIndex + 1);
        }
        lambda.invokeInterface(interfaceType, interfaceMethodName);

        lambda.iinc(iLocal, 1);
        lambda.goTo(loop);

        lambda.visitLabel(end);
        lambda.returnValue();
        lambda.endMethod();

        return writer.toByteArray();
    }

    private static Type[] prefix(Type prefix, Type[] types) {
        Type[] newTypes = new Type[types.length + 1];
        newTypes[0] = prefix;
        System.arraycopy(types, 0, newTypes, 1, types.length);
        return newTypes;
    }
}
