package com.kneelawk.commonevents.impl.scan;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import com.kneelawk.commonevents.api.adapter.util.AdapterUtils;
import com.kneelawk.commonevents.impl.CELog;
import com.kneelawk.commonevents.impl.gen.WeakReferenceListenerGenerator;

public class ListenerBuilder {
    @SuppressWarnings("unchecked")
    public static <T, U extends T> U buildInstanceListener(Class<T> callbackInterface, Object listener,
                                                           Method listenerMethod) {
        Class<?> listenerClass = listener.getClass();

        Methods methods = verifyMethods(listenerClass, listenerMethod, callbackInterface);

        try {
            MethodHandle handle =
                AdapterUtils.LOOKUP.findVirtual(listenerClass, listenerMethod.getName(), methods.actualMethodType());

            return (U) callbackInterface.cast(
                LambdaMetafactory.metafactory(AdapterUtils.LOOKUP, methods.interfaceMethod().getName(),
                    MethodType.methodType(callbackInterface, listenerClass), methods.expectedMethodType(), handle,
                    methods.expectedMethodType()).getTarget().invoke(listener));
        } catch (Throwable t) {
            throw handleError(callbackInterface, listenerClass, listenerMethod, methods, t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, U extends T> U buildStaticListener(Class<T> callbackInterface, Class<?> listenerClass,
                                                         Method listenerMethod) {
        Methods methods = verifyMethods(listenerClass, listenerMethod, callbackInterface);

        try {
            MethodHandle handle =
                AdapterUtils.LOOKUP.findStatic(listenerClass, listenerMethod.getName(), methods.actualMethodType());

            return (U) callbackInterface.cast(
                LambdaMetafactory.metafactory(AdapterUtils.LOOKUP, methods.interfaceMethod().getName(),
                    MethodType.methodType(callbackInterface), methods.expectedMethodType(), handle,
                    methods.expectedMethodType()).getTarget().invoke());
        } catch (Throwable t) {
            throw handleError(callbackInterface, listenerClass, listenerMethod, methods, t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, U extends T> U buildWeakListener(Class<T> callbackInterface, Class<?> listenerClass,
                                                       WeakReference<?> listener,
                                                       Method listenerMethod, @Nullable Object defaultReturn) {
        Methods methods = verifyMethods(listenerClass, listenerMethod, callbackInterface);

        try {
            return (U) WeakReferenceListenerGenerator.defineWrapper(callbackInterface, listenerClass, listenerMethod,
                listener, defaultReturn);
        } catch (Throwable t) {
            throw handleError(callbackInterface, listenerClass, listenerMethod, methods, t);
        }
    }

    private static @NotNull Methods verifyMethods(Class<?> listenerClass, Method listenerMethod,
                                                  Class<?> callbackInterface) {
        Method interfaceMethod = AdapterUtils.getSingularMethod(callbackInterface);
        if (interfaceMethod == null) throw new IllegalArgumentException(
            "Tried to listen to callback interface " + callbackInterface + " which is not a functional interface");

        MethodType expectedMethodType =
            MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes());
        MethodType actualMethodType =
            MethodType.methodType(listenerMethod.getReturnType(), listenerMethod.getParameterTypes());
        Methods methods = new Methods(interfaceMethod, expectedMethodType, actualMethodType);

        checkReturnTypes(callbackInterface, listenerClass, listenerMethod, methods);
        return methods;
    }

    private static void checkReturnTypes(Class<?> callbackInterface, Class<?> listenerClass, Method listenerMethod,
                                         Methods methods) {
        if (!methods.expectedMethodType().returnType().isAssignableFrom(methods.actualMethodType().returnType())) {
            String callbackClassName = callbackInterface.getName().replace('.', '/');
            String listenerClassName = listenerClass.getName().replace('.', '/');
            Type expectedType = AdapterUtils.getMethodType(methods.expectedMethodType());
            Type actualType = AdapterUtils.getMethodType(methods.actualMethodType());
            CELog.LOGGER.warn(
                "[Common Events] Callback listener {}.{}{} has return type that is incompatible with callback interface {}.{}{}. " +
                    "The associated event may throw a ClassCastException when called.", listenerClassName,
                listenerMethod.getName(), actualType, callbackClassName, methods.interfaceMethod().getName(),
                expectedType);
        }
    }

    private static RuntimeException handleError(Class<?> callbackInterface, Class<?> listenerClass,
                                                Method listenerMethod, Methods methods, Throwable t) {
        String callbackClassName = callbackInterface.getName().replace('.', '/');
        String listenerClassName = listenerClass.getName().replace('.', '/');
        Type expectedType = AdapterUtils.getMethodType(methods.expectedMethodType());
        Type actualType = AdapterUtils.getMethodType(methods.actualMethodType());
        return new RuntimeException(
            "Error connecting listener method " + listenerClassName + "." + listenerMethod.getName() + actualType +
                " with callback interface " + callbackClassName + "." + methods.interfaceMethod().getName() +
                expectedType, t);
    }

    private record Methods(Method interfaceMethod, MethodType expectedMethodType, MethodType actualMethodType) {}
}
