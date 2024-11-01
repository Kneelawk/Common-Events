package com.kneelawk.commonevents.impl.gen;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;

public class WeakCallbackWrapperGenerator {
    private static final ClassGenerator<Spec> GENERATOR =
        new ClassGenerator<>("com.kneelawk.commonevents.impl.gen.impl.$CommonEvents_Generated$.WeakCallbackWrapper",
            "event-weak-callback-generator", WeakCallbackWrapperGenerator::generateCode);

    private static final Type cleanerHolderType =
        Type.getObjectType("com/kneelawk/commonevents/impl/event/CleanerHolder");
    private static final String cleanerName = "CLEANER";

    private record Spec(Class<?> interfaceClass, boolean ret) {}

    public interface Factory<T> {
        T newInstance(T impl, Consumer<Object> cleaner, @Nullable Object defaultReturn);
    }

    private static byte[] generateCode(Spec spec, Type beingDefined) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        Type interfaceType = Type.getType(spec.interfaceClass());
        java.lang.reflect.Method interfaceMethod = AdapterUtils.getSingularMethod(spec.interfaceClass());
        assert interfaceMethod != null;
        Method interfaceMethodName = Method.getMethod(interfaceMethod);
        Type interfaceMethodType = Type.getType(interfaceMethod);
        Type[] interfaceMethodArgs = interfaceMethodType.getArgumentTypes();
        Type interfaceMethodReturn = interfaceMethodType.getReturnType();
        Class<?>[] interfaceMethodExceptionClasses = interfaceMethod.getExceptionTypes();
        Type[] interfaceMethodExceptions = new Type[interfaceMethodExceptionClasses.length];
        for (int i = 0; i < interfaceMethodExceptionClasses.length; i++) {
            interfaceMethodExceptions[i] = Type.getType(interfaceMethodExceptionClasses[i]);
        }

        Type objectType = Type.getType(Object.class);
        Method objectInit = Method.getMethod("void <init> ()");

        Type weakType = Type.getType(WeakReference.class);
        Method weakInit = new Method("<init>", Type.VOID_TYPE, new Type[]{objectType});
        Method weakGet = new Method("get", objectType, new Type[0]);

        Type runnableType = Type.getType(Runnable.class);
        Type consumerType = Type.getType(Consumer.class);
        Type cleanerType = Type.getType(Cleaner.class);
        Type cleanableType = Type.getType(Cleaner.Cleanable.class);
        Method cleanerRegister = new Method("register", cleanableType, new Type[]{objectType, runnableType});

        Method cleanerHolderInit = new Method("<init>", Type.VOID_TYPE, new Type[]{consumerType, objectType});

        writer.visit(AdapterUtils.JAVA_VERSION, Opcodes.ACC_PUBLIC, beingDefined.getInternalName(), null,
            objectType.getInternalName(), new String[]{interfaceType.getInternalName()});
        writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "wrapped", weakType.getDescriptor(), null, null)
            .visitEnd();
        if (spec.ret()) {
            writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "defaultReturn",
                interfaceMethodReturn.getDescriptor(),
                null, null).visitEnd();
        }

        Type[] initArgs;
        if (spec.ret()) {
            initArgs = new Type[]{interfaceType, consumerType, interfaceMethodReturn};
        } else {
            initArgs = new Type[]{interfaceType, consumerType};
        }
        Method initMethod = new Method("<init>", Type.VOID_TYPE, initArgs);
        GeneratorAdapter constructor = new GeneratorAdapter(Opcodes.ACC_PUBLIC, initMethod, null, null, writer);
        constructor.loadThis();
        constructor.invokeConstructor(objectType, objectInit);

        constructor.loadThis();
        constructor.newInstance(weakType);
        constructor.dup();
        constructor.loadArg(0);
        constructor.invokeConstructor(weakType, weakInit);
        constructor.putField(beingDefined, "wrapped", weakType);

        constructor.getStatic(cleanerHolderType, cleanerName, cleanerType);
        constructor.loadArg(0);
        constructor.newInstance(cleanerHolderType);
        constructor.dup();
        constructor.loadArg(1);
        constructor.loadThis();
        constructor.invokeConstructor(cleanerHolderType, cleanerHolderInit);
        constructor.invokeVirtual(cleanerType, cleanerRegister);
        constructor.pop();

        if (spec.ret()) {
            constructor.loadThis();
            constructor.loadArg(2);
            constructor.putField(beingDefined, "defaultReturn", interfaceMethodReturn);
        }

        constructor.returnValue();
        constructor.endMethod();

        GeneratorAdapter impl =
            new GeneratorAdapter(Opcodes.ACC_PUBLIC, interfaceMethodName, null, interfaceMethodExceptions, writer);
        impl.loadThis();
        impl.getField(beingDefined, "wrapped", weakType);
        impl.invokeVirtual(weakType, weakGet);
        impl.checkCast(interfaceType);
        int local = impl.newLocal(interfaceType);
        impl.storeLocal(local);

        Label after = impl.newLabel();
        impl.loadLocal(local);
        impl.ifNull(after);

        impl.loadLocal(local);
        for (int i = 0; i < interfaceMethodArgs.length; i++) {
            impl.loadArg(i);
        }
        impl.invokeInterface(interfaceType, interfaceMethodName);
        if (spec.ret()) {
            impl.returnValue();
        }

        impl.mark(after);
        if (spec.ret()) {
            impl.loadThis();
            impl.getField(beingDefined, "defaultReturn", interfaceMethodReturn);
        }
        impl.returnValue();
        impl.endMethod();

        return writer.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> Factory<T> defineWrapper(Class<T> interfaceClass) {
        if (!interfaceClass.isInterface())
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");

        java.lang.reflect.Method interfaceMethod = AdapterUtils.getSingularMethod(interfaceClass);
        if (interfaceMethod == null)
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");
        Class<?> retClass = interfaceMethod.getReturnType();

        boolean retVoid = retClass == void.class;

        try {
            Class<T> tClass = (Class<T>) GENERATOR.getOrCreateClass(new Spec(interfaceClass, !retVoid));

            if (retVoid) {
                Constructor<T> constructor = tClass.getConstructor(interfaceClass, Consumer.class);
                return (impl, cleaner, defaultReturn) -> {
                    try {
                        return constructor.newInstance(impl, cleaner);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new AssertionError(e);
                    }
                };
            } else {
                Constructor<T> constructor = tClass.getConstructor(interfaceClass, Consumer.class, retClass);
                return (impl, cleaner, defaultReturn) -> {
                    if (defaultReturn == null && retClass.isPrimitive()) {
                        defaultReturn = defaultValue(retClass);
                    }
                    try {
                        return constructor.newInstance(impl, cleaner, defaultReturn);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new AssertionError(e);
                    }
                };
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private static Object defaultValue(Class<?> clazz) {
        if (clazz == boolean.class) {
            return false;
        } else if (clazz == char.class) {
            return '\0';
        } else if (clazz == byte.class) {
            return 0;
        } else if (clazz == short.class) {
            return 0;
        } else if (clazz == int.class) {
            return 0;
        } else if (clazz == float.class) {
            return 0f;
        } else if (clazz == long.class) {
            return 0;
        } else if (clazz == double.class) {
            return 0d;
        }
        return null;
    }
}
