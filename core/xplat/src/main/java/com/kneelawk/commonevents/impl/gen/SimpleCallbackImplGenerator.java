package com.kneelawk.commonevents.impl.gen;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;

public class SimpleCallbackImplGenerator {
    private static final ClassGenerator<Spec> GENERATOR =
        new ClassGenerator<>("com.kneelawk.commonevents.impl.gen.impl.$CommonEvents_Generated$.SimpleCallbackImpl",
            "event-simple-implementation-generator", SimpleCallbackImplGenerator::generateClass);

    private record Spec(Class<?> interfaceClass, boolean catchErrors) {}

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

    @SuppressWarnings("unchecked")
    public static <T> Function<T[], T> defineSimple(Class<? super T> interfaceClass,
                                                    @Nullable Consumer<Exception> catchErrors) {
        if (!interfaceClass.isInterface())
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");

        java.lang.reflect.Method interfaceMethod = AdapterUtils.getSingularMethod(interfaceClass);
        if (interfaceMethod == null)
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");

        if (!Void.TYPE.equals(interfaceMethod.getReturnType())) throw new IllegalArgumentException(
            interfaceClass.getName() +
                " is not a simple functional interface. Simple functional interfaces must not return anything.");

        try {
            Class<Function<T[], T>> clazz =
                (Class<Function<T[], T>>) GENERATOR.getOrCreateClass(new Spec(interfaceClass, catchErrors != null));

            if (catchErrors != null) {
                return clazz.getConstructor(Consumer.class).newInstance(catchErrors);
            } else {
                return clazz.getConstructor().newInstance();
            }
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Unable to generate simple implementation for " + interfaceClass.getName(), e);
        }
    }

    private static byte[] generateClass(Spec spec, Type beingDefined) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        Type interfaceType = Type.getType(spec.interfaceClass());
        Type interfaceArrayType = Type.getType("[" + interfaceType.getDescriptor());
        java.lang.reflect.Method interfaceMethod = AdapterUtils.getSingularMethod(spec.interfaceClass());
        assert interfaceMethod != null;
        Method interfaceMethodName = Method.getMethod(interfaceMethod);
        Type interfaceMethodType = Type.getType(interfaceMethod);
        Type[] interfaceMethodArgs = interfaceMethodName.getArgumentTypes();

        Type functionType = Type.getType(Function.class);
        Type consumerType = Type.getType(Consumer.class);
        Type exceptionType = Type.getType(Exception.class);
        Type objectType = Type.getType(Object.class);

        Method applyMethodName =
            new Method("apply", interfaceType, new Type[]{interfaceArrayType});
        Type[] capturedArgumentTypes;
        if (spec.catchErrors()) {
            capturedArgumentTypes = new Type[]{interfaceArrayType, consumerType};
        } else {
            capturedArgumentTypes = new Type[]{interfaceArrayType};
        }
        Method capturedMethodName =
            new Method(interfaceMethodName.getName(), interfaceType, capturedArgumentTypes);
        Method consumerAccept = new Method("accept", Type.VOID_TYPE, new Type[]{objectType});
        Method objectInit = Method.getMethod("void <init> ()");

        String signature = objectType.getDescriptor() + "L" + functionType.getInternalName() + "<" +
            interfaceArrayType.getDescriptor() + interfaceType.getDescriptor() + ">;";

        writer.visit(AdapterUtils.JAVA_VERSION, Opcodes.ACC_PUBLIC, beingDefined.getInternalName(), signature,
            objectType.getInternalName(), new String[]{functionType.getInternalName()});
        writer.visitInnerClass("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup",
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC);

        if (spec.catchErrors()) {
            writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "exceptionHandler",
                consumerType.getDescriptor(),
                "L" + consumerType.getInternalName() + "<" + exceptionType.getDescriptor() + ">;", null).visitEnd();
        }

        Method initMethod;
        String initSignature;
        if (spec.catchErrors()) {
            initMethod = new Method("<init>", Type.VOID_TYPE, new Type[]{consumerType});
            initSignature = "(L" + consumerType.getInternalName() + "<" + exceptionType.getDescriptor() + ">;)V";
        } else {
            initMethod = objectInit;
            initSignature = null;
        }
        GeneratorAdapter constructor =
            new GeneratorAdapter(Opcodes.ACC_PUBLIC, initMethod, initSignature, null, writer);
        constructor.loadThis();
        constructor.invokeConstructor(objectType, objectInit);
        if (spec.catchErrors()) {
            constructor.loadThis();
            constructor.loadArg(0);
            constructor.putField(beingDefined, "exceptionHandler", consumerType);
        }
        constructor.returnValue();
        constructor.endMethod();

        Type[] suffixArgs;
        if (spec.catchErrors()) {
            suffixArgs = prefix(consumerType, interfaceMethodArgs);
        } else {
            suffixArgs = interfaceMethodArgs;
        }
        Method lambdaMethodName =
            new Method("lambda$apply$0", Type.VOID_TYPE, prefix(interfaceArrayType, suffixArgs));

        GeneratorAdapter apply = new GeneratorAdapter(Opcodes.ACC_PUBLIC, applyMethodName, null, null, writer);
        apply.loadArg(0);
        if (spec.catchErrors()) {
            apply.loadThis();
            apply.getField(beingDefined, "exceptionHandler", consumerType);
        }
        apply.invokeDynamic(capturedMethodName.getName(), capturedMethodName.getDescriptor(), LMF_HANDLE,
            interfaceMethodType,
            new Handle(Opcodes.H_INVOKESTATIC, beingDefined.getInternalName(), lambdaMethodName.getName(),
                lambdaMethodName.getDescriptor(), false), interfaceMethodType);
        apply.returnValue();
        apply.endMethod();

        GeneratorAdapter applyBridge =
            new GeneratorAdapter(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE,
                new Method("apply", objectType, new Type[]{objectType}), null, null, writer);
        applyBridge.loadThis();
        applyBridge.loadArg(0);
        applyBridge.checkCast(interfaceArrayType);
        applyBridge.invokeVirtual(beingDefined, applyMethodName);
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

        Label loop = lambda.mark();
        Label end = lambda.newLabel();
        lambda.loadLocal(iLocal);
        lambda.loadLocal(lenLocal);
        lambda.ifICmp(GeneratorAdapter.GE, end);

        Label tryStart = null;
        Label tryEnd = null;
        Label tryAfter = null;
        if (spec.catchErrors()) {
            tryStart = lambda.mark();
            tryEnd = lambda.newLabel();
            tryAfter = lambda.newLabel();
        }

        lambda.loadArg(0);
        lambda.loadLocal(iLocal);
        lambda.arrayLoad(interfaceType);
        for (int argIndex = 0; argIndex < interfaceMethodArgs.length; argIndex++) {
            // argIndex + 1 because the first arg is the array of callbacks
            lambda.loadArg(argIndex + (spec.catchErrors() ? 2 : 1));
        }
        lambda.invokeInterface(interfaceType, interfaceMethodName);

        if (spec.catchErrors()) {
            lambda.mark(tryEnd);
            lambda.goTo(tryAfter);

            lambda.catchException(tryStart, tryEnd, exceptionType);
            lambda.loadArg(1);
            lambda.swap();
            lambda.invokeInterface(consumerType, consumerAccept);

            lambda.mark(tryAfter);
        }

        lambda.iinc(iLocal, 1);
        lambda.goTo(loop);

        lambda.mark(end);
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
