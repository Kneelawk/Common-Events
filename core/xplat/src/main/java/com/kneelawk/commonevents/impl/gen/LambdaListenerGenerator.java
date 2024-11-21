package com.kneelawk.commonevents.impl.gen;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;

public class LambdaListenerGenerator {
    private static final ClassGenerator<Spec> GENERATOR =
        new ClassGenerator<>("com.kneelawk.commonevents.impl.gen.impl.$CommonEvents_Generated$.LambdaListener",
            "event-lambda-listener-generator", LambdaListenerGenerator::generateCode);

    private record Spec(Class<?> interfaceClass, Type implType, Method implMethod, boolean staticMethod,
                        boolean interfaceMethod) {}

    private static byte[] generateCode(Spec spec, Type beingDefined) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        Type implType = spec.implType();
        Method implMethod = spec.implMethod();
        Type[] implMethodArgs = implMethod.getArgumentTypes();
        Type interfaceType = Type.getType(spec.interfaceClass());
        java.lang.reflect.Method interfaceMethodRef = AdapterUtils.getSingularMethod(spec.interfaceClass());
        assert interfaceMethodRef != null;
        Method interfaceMethod = Method.getMethod(interfaceMethodRef);
        Type interfaceMethodReturn = interfaceMethod.getReturnType();
        Class<?>[] interfaceMethodExceptionClasses = interfaceMethodRef.getExceptionTypes();
        Type[] interfaceMethodExceptions = new Type[interfaceMethodExceptionClasses.length];
        for (int i = 0; i < interfaceMethodExceptionClasses.length; i++) {
            interfaceMethodExceptions[i] = Type.getType(interfaceMethodExceptionClasses[i]);
        }

        Type objectType = Type.getType(Object.class);
        Method objectInit = Method.getMethod("void <init> ()");

        writer.visit(AdapterUtils.JAVA_VERSION, Opcodes.ACC_PUBLIC, beingDefined.getInternalName(), null,
            objectType.getInternalName(), new String[]{interfaceType.getInternalName()});
        if (!spec.staticMethod()) {
            writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "wrapped", implType.getDescriptor(), null, null)
                .visitEnd();
        }

        Type[] initArgs;
        if (spec.staticMethod()) {
            initArgs = new Type[0];
        } else {
            initArgs = new Type[]{implType};
        }
        Method initMethod = new Method("<init>", Type.VOID_TYPE, initArgs);
        GeneratorAdapter constructor = new GeneratorAdapter(Opcodes.ACC_PUBLIC, initMethod, null, null, writer);
        constructor.loadThis();
        constructor.invokeConstructor(objectType, objectInit);

        if (!spec.staticMethod()) {
            constructor.loadThis();
            constructor.loadArg(0);
            constructor.putField(beingDefined, "wrapped", implType);
        }

        constructor.returnValue();
        constructor.endMethod();

        GeneratorAdapter impl =
            new GeneratorAdapter(Opcodes.ACC_PUBLIC, interfaceMethod, null, interfaceMethodExceptions, writer);
        if (!spec.staticMethod()) {
            impl.loadThis();
            impl.getField(beingDefined, "wrapped", implType);
        }

        for (int i = 0; i < implMethodArgs.length; i++) {
            impl.loadArg(i);
        }

        if (spec.staticMethod()) {
            impl.invokeStatic(implType, implMethod);
        } else {
            if (spec.interfaceMethod()) {
                impl.invokeInterface(implType, implMethod);
            } else {
                impl.invokeVirtual(implType, implMethod);
            }
        }

        if (!interfaceMethodReturn.equals(Type.VOID_TYPE)) {
            impl.checkCast(interfaceMethodReturn);
        }

        impl.returnValue();
        impl.endMethod();

        return writer.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> T defineWrapper(Class<T> interfaceClass, Class<?> implClass, java.lang.reflect.Method implMethod,
                                      @Nullable Object impl) {
        try {
            Class<T> tClass = (Class<T>) GENERATOR.getOrCreateClass(
                new Spec(interfaceClass, Type.getType(implClass), Method.getMethod(implMethod), impl == null,
                    implClass.isInterface()));

            if (impl == null) {
                Constructor<T> constructor = tClass.getConstructor();
                return constructor.newInstance();
            } else {
                Constructor<T> constructor = tClass.getConstructor(implClass);
                return constructor.newInstance(impl);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
