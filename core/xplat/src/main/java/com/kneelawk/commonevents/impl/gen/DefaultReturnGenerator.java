package com.kneelawk.commonevents.impl.gen;

import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;

public class DefaultReturnGenerator {
    private record Spec(Class<?> interfaceClass) {}

    private static final ClassGenerator<Spec> GENERATOR =
        new ClassGenerator<>("com.kneelawk.commonevents.impl.gen.impl.$CommonEvents_Generated$.DefaultReturn",
            "event-default-return-generator", DefaultReturnGenerator::generate);

    private static byte[] generate(Spec spec, Type beingDefined) {
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

        Type objectType = Type.getType(Object.class);
        Method objectInit = Method.getMethod("void <init> ()");

        writer.visit(AdapterUtils.JAVA_VERSION, Opcodes.ACC_PUBLIC, beingDefined.getInternalName(), null,
            objectType.getInternalName(), new String[]{interfaceType.getInternalName()});
        writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "defaultReturn",
            interfaceMethodReturn.getDescriptor(), null, null).visitEnd();

        Method initMethod = new Method("<init>", Type.VOID_TYPE, new Type[]{interfaceMethodReturn});
        GeneratorAdapter constructor = new GeneratorAdapter(Opcodes.ACC_PUBLIC, initMethod, null, null, writer);
        constructor.loadThis();
        constructor.invokeConstructor(objectType, objectInit);

        constructor.loadThis();
        constructor.loadArg(0);
        constructor.putField(beingDefined, "defaultReturn", interfaceMethodReturn);

        constructor.returnValue();
        constructor.endMethod();

        GeneratorAdapter impl =
            new GeneratorAdapter(Opcodes.ACC_PUBLIC, interfaceMethodName, null, interfaceMethodExceptions, writer);
        impl.loadThis();
        impl.getField(beingDefined, "defaultReturn", interfaceMethodReturn);
        impl.returnValue();
        impl.endMethod();

        return writer.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> T defineImpl(Class<T> interfaceClass, @Nullable Object defaultReturn) {
        if (!interfaceClass.isInterface())
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");

        java.lang.reflect.Method interfaceMethod = AdapterUtils.getSingularMethod(interfaceClass);
        if (interfaceMethod == null)
            throw new IllegalArgumentException(interfaceClass.getName() + " is not a functional interface");
        Class<?> retClass = interfaceMethod.getReturnType();
        if (retClass == void.class)
            throw new IllegalArgumentException(
                interfaceClass.getName() + "." + interfaceMethod.getName() + " returns void");

        if (defaultReturn == null && retClass.isPrimitive()) {
            defaultReturn = defaultValue(retClass);
        }

        try {
            Class<T> clazz = (Class<T>) GENERATOR.getOrCreateClass(new Spec(interfaceClass));
            return clazz.getConstructor(retClass).newInstance(defaultReturn);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object defaultValue(Class<?> clazz) {
        if (clazz == boolean.class) {
            return false;
        } else if (clazz == char.class) {
            return '\0';
        } else if (clazz == byte.class) {
            return (byte) 0;
        } else if (clazz == short.class) {
            return (short) 0;
        } else if (clazz == int.class) {
            return 0;
        } else if (clazz == float.class) {
            return 0f;
        } else if (clazz == long.class) {
            return 0L;
        } else if (clazz == double.class) {
            return 0d;
        }
        return null;
    }
}
