package com.kneelawk.commonevents.impl.gen;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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

    private record Spec(Class<?> interfaceClass, Type implType, Method implMethod, boolean ret) {}

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
        Method weakGet = new Method("get", objectType, new Type[0]);

        writer.visit(AdapterUtils.JAVA_VERSION, Opcodes.ACC_PUBLIC, beingDefined.getInternalName(), null,
            objectType.getInternalName(), new String[]{interfaceType.getInternalName()});
        writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "wrapped", weakType.getDescriptor(),
            "L" + weakType.getInternalName() + "<" + spec.implType().getDescriptor() + ">;", null).visitEnd();
        if (spec.ret()) {
            writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "defaultReturn",
                interfaceMethodReturn.getDescriptor(),
                null, null).visitEnd();
        }

        Type[] initArgs;
        if (spec.ret()) {
            initArgs = new Type[]{weakType, interfaceMethodReturn};
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

        if (spec.ret()) {
            constructor.loadThis();
            constructor.loadArg(1);
            constructor.putField(beingDefined, "defaultReturn", interfaceMethodReturn);
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
        for (int i = 0; i < interfaceMethodArgs.length; i++) {
            impl.loadArg(i);
        }
        impl.invokeVirtual(spec.implType(), spec.implMethod());
        if (spec.ret()) {
            impl.checkCast(interfaceMethodReturn);
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
    public static <T> T defineWrapper(Class<T> interfaceClass, Class<?> implClass, java.lang.reflect.Method implMethod,
                                      WeakReference<?> impl, Object defaultReturn) {
        if (!interfaceClass.isInterface())
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");

        java.lang.reflect.Method interfaceMethod = AdapterUtils.getSingularMethod(interfaceClass);
        if (interfaceMethod == null)
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");
        Class<?> retClass = interfaceMethod.getReturnType();
        boolean retVoid = retClass == void.class;

        Class<?>[] interfaceParams = interfaceMethod.getParameterTypes();
        Class<?>[] implParams = implMethod.getParameterTypes();
        if (interfaceParams.length != implParams.length) throw new IllegalArgumentException(
            "Implementation method " + implMethod + " does not match callback interface method " + interfaceMethod);
        for (int i = 0; i < interfaceParams.length; i++) {
            if (!implParams[i].isAssignableFrom(interfaceParams[i])) throw new IllegalArgumentException(
                "Implementation method " + implMethod + " does not match callback interface method " + interfaceMethod);
        }
        if (!retVoid && implMethod.getReturnType() == void.class) {
            throw new IllegalArgumentException(
                "Implementation method " + implMethod + " returns 'void' but callback interface method " +
                    interfaceMethod + " does not.");
        }

        if (!retVoid && defaultReturn == null && retClass.isPrimitive()) {
            defaultReturn = defaultValue(retClass);
        }

        try {
            Class<T> tClass = (Class<T>) GENERATOR.getOrCreateClass(
                new Spec(interfaceClass, Type.getType(implClass), Method.getMethod(implMethod), !retVoid));

            if (retVoid) {
                Constructor<T> constructor = tClass.getConstructor(WeakReference.class);
                return constructor.newInstance(impl);
            } else {
                Constructor<T> constructor = tClass.getConstructor(WeakReference.class, retClass);
                return constructor.newInstance(impl, defaultReturn);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
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
