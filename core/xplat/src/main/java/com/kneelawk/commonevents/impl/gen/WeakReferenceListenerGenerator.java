package com.kneelawk.commonevents.impl.gen;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;

public class WeakReferenceListenerGenerator {
    private static final ClassGenerator<Spec> GENERATOR =
        new ClassGenerator<>("com.kneelawk.commonevents.impl.gen.impl.$CommonEvents_Generated$.WeakReferenceListener",
            "event-weak-listener-generator", WeakReferenceListenerGenerator::generateCode);

    private record Spec(Class<?> interfaceClass, Type implType, Method implMethod, boolean defaultImpl) {}

    private static byte[] generateCode(Spec spec, Type beingDefined) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        Type interfaceType = Type.getType(spec.interfaceClass());
        java.lang.reflect.Method interfaceMethod = AdapterUtils.getSingularMethod(spec.interfaceClass());
        assert interfaceMethod != null;
        Method interfaceMethodName = Method.getMethod(interfaceMethod);
        Type interfaceMethodType = Type.getType(interfaceMethod);
        Type interfaceMethodReturn = interfaceMethodType.getReturnType();
        Class<?>[] interfaceMethodExceptionClasses = interfaceMethod.getExceptionTypes();
        Type[] interfaceMethodExceptions = new Type[interfaceMethodExceptionClasses.length];
        for (int i = 0; i < interfaceMethodExceptionClasses.length; i++) {
            interfaceMethodExceptions[i] = Type.getType(interfaceMethodExceptionClasses[i]);
        }
        Type[] implMethodArgs = spec.implMethod().getArgumentTypes();

        Type objectType = Type.getType(Object.class);
        Method objectInit = Method.getMethod("void <init> ()");

        Type weakType = Type.getType(WeakReference.class);
        Method weakGet = new Method("get", objectType, new Type[0]);

        writer.visit(AdapterUtils.JAVA_VERSION, Opcodes.ACC_PUBLIC, beingDefined.getInternalName(), null,
            objectType.getInternalName(), new String[]{interfaceType.getInternalName()});
        writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "wrapped", weakType.getDescriptor(),
            "L" + weakType.getInternalName() + "<" + spec.implType().getDescriptor() + ">;", null).visitEnd();
        if (spec.defaultImpl()) {
            writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "defaultImpl",
                interfaceType.getDescriptor(), null, null).visitEnd();
        }

        Type[] initArgs;
        if (spec.defaultImpl()) {
            initArgs = new Type[]{weakType, interfaceType};
        } else {
            initArgs = new Type[]{weakType};
        }
        Method initMethod = new Method("<init>", Type.VOID_TYPE, initArgs);
        GeneratorAdapter constructor = new GeneratorAdapter(Opcodes.ACC_PUBLIC, initMethod,
            "(L" + weakType.getInternalName() + "<" + spec.implType().getDescriptor() + ">;)V", null, writer);
        constructor.loadThis();
        constructor.invokeConstructor(objectType, objectInit);

        constructor.loadThis();
        constructor.loadArg(0);
        constructor.putField(beingDefined, "wrapped", weakType);

        if (spec.defaultImpl()) {
            constructor.loadThis();
            constructor.loadArg(1);
            constructor.putField(beingDefined, "defaultImpl", interfaceType);
        }

        constructor.returnValue();
        constructor.endMethod();

        GeneratorAdapter impl =
            new GeneratorAdapter(Opcodes.ACC_PUBLIC, interfaceMethodName, null, interfaceMethodExceptions, writer);
        impl.loadThis();
        impl.getField(beingDefined, "wrapped", weakType);
        impl.invokeVirtual(weakType, weakGet);
        impl.checkCast(spec.implType());
        int local = impl.newLocal(spec.implType());
        impl.storeLocal(local);

        Label after = impl.newLabel();
        impl.loadLocal(local);
        impl.ifNull(after);

        impl.loadLocal(local);
        for (int i = 0; i < implMethodArgs.length; i++) {
            impl.loadArg(i);
        }
        impl.invokeVirtual(spec.implType(), spec.implMethod());
        if (interfaceMethodReturn != Type.VOID_TYPE) {
            impl.checkCast(interfaceMethodReturn);
        }
        impl.returnValue();

        impl.mark(after);
        if (spec.defaultImpl()) {
            impl.loadThis();
            impl.getField(beingDefined, "defaultImpl", interfaceType);
            int interfaceMethodArgCount = interfaceMethodName.getArgumentTypes().length;
            for (int i = 0; i < interfaceMethodArgCount; i++) {
                impl.loadArg(i);
            }
            impl.invokeVirtual(interfaceType, interfaceMethodName);
            if (interfaceMethodReturn != Type.VOID_TYPE) {
                impl.checkCast(interfaceMethodReturn);
            }
        }
        impl.returnValue();
        impl.endMethod();

        return writer.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> T defineWrapper(Class<T> interfaceClass, Class<?> implClass, java.lang.reflect.Method implMethod,
                                      WeakReference<?> impl, @Nullable T defaultImpl) {
        if (!interfaceClass.isInterface())
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");

        java.lang.reflect.Method interfaceMethod = AdapterUtils.getSingularMethod(interfaceClass);
        if (interfaceMethod == null)
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");

        try {
            Class<T> tClass = (Class<T>) GENERATOR.getOrCreateClass(
                new Spec(interfaceClass, Type.getType(implClass), Method.getMethod(implMethod), defaultImpl != null));

            if (defaultImpl != null) {
                Constructor<T> constructor = tClass.getConstructor(WeakReference.class, interfaceClass);
                return constructor.newInstance(impl, defaultImpl);
            } else {
                Constructor<T> constructor = tClass.getConstructor(WeakReference.class);
                return constructor.newInstance(impl);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
