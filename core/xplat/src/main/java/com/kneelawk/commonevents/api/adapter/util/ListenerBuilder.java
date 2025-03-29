package com.kneelawk.commonevents.api.adapter.util;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import com.kneelawk.commonevents.api.adapter.BuilderSettings;
import com.kneelawk.commonevents.api.adapter.scan.BadListenerException;
import com.kneelawk.commonevents.impl.CELog;
import com.kneelawk.commonevents.impl.gen.LambdaListenerGenerator;
import com.kneelawk.commonevents.impl.gen.WeakReferenceListenerGenerator;

/**
 * Utility class for creating callback interface implementations out of method references.
 */
public final class ListenerBuilder {
    private ListenerBuilder() {}

    /**
     * Creates a callback interface instance from the given object instance and method reference on that object.
     *
     * @param callbackInterface the callback interface to implement.
     * @param listener          the listener object which holds the method that will implement the callback interface.
     * @param listenerMethod    the reference to the method that will implement the callback interface.
     * @param settings          additional settings for how the interface instance should be implemented.
     * @param <T>               the type of callback interface returned.
     * @return the newly created callback interface instance.
     * @throws BadListenerException if the listener method or callback interface make it impossible to create a valid
     *                              implementation.
     */
    public static <T> T buildInstanceListener(Class<T> callbackInterface, Object listener, Method listenerMethod,
                                              BuilderSettings settings) throws BadListenerException {
        Class<?> listenerClass = listener.getClass();

        Methods methods = verifyMethods(listenerClass, listenerMethod, callbackInterface, settings);

        try {
            if (listenerMethod.getParameterCount() == methods.interfaceMethod().getParameterCount()) {
                // we use lambda metafactory if we can, because it is probably more efficient
                MethodHandle handle =
                    AdapterUtils.LOOKUP.findVirtual(listenerClass, listenerMethod.getName(),
                        methods.actualMethodType());

                return callbackInterface.cast(
                    LambdaMetafactory.metafactory(AdapterUtils.LOOKUP, methods.interfaceMethod().getName(),
                        MethodType.methodType(callbackInterface, listenerClass), methods.expectedMethodType(), handle,
                        methods.expectedMethodType()).getTarget().invoke(listener));
            } else {
                return LambdaListenerGenerator.defineWrapper(callbackInterface, listenerClass, listenerMethod,
                    listener);
            }
        } catch (Throwable t) {
            throw handleError(callbackInterface, listenerClass, listenerMethod, methods, t);
        }
    }

    /**
     * Creates a callback interface instance from the given static method on the given class.
     *
     * @param callbackInterface the callback interface to implement.
     * @param listenerClass     the class that the static listener method belongs to.
     * @param listenerMethod    the reference to the static method that will implement the callback interface.
     * @param settings          additional settings for how the interface instance should be implemented.
     * @param <T>               the type of callback interface returned.
     * @return the newly created callback interface.
     * @throws BadListenerException if the listener method or callback interface make it impossible to create a valid
     *                              implementation.
     */
    public static <T> T buildStaticListener(Class<T> callbackInterface, Class<?> listenerClass, Method listenerMethod,
                                            BuilderSettings settings) throws BadListenerException {
        Methods methods = verifyMethods(listenerClass, listenerMethod, callbackInterface, settings);

        try {
            if (listenerMethod.getParameterCount() == methods.interfaceMethod().getParameterCount()) {
                // we use lambda metafactory if we can, because it is probably more efficient
                MethodHandle handle =
                    AdapterUtils.LOOKUP.findStatic(listenerClass, listenerMethod.getName(), methods.actualMethodType());

                return callbackInterface.cast(
                    LambdaMetafactory.metafactory(AdapterUtils.LOOKUP, methods.interfaceMethod().getName(),
                        MethodType.methodType(callbackInterface), methods.expectedMethodType(), handle,
                        methods.expectedMethodType()).getTarget().invoke());
            } else {
                return LambdaListenerGenerator.defineWrapper(callbackInterface, listenerClass, listenerMethod, null);
            }
        } catch (Throwable t) {
            throw handleError(callbackInterface, listenerClass, listenerMethod, methods, t);
        }
    }

    /**
     * Creates a callback interface instance from the given weak reference to an object instance and method reference
     * on that object.
     *
     * @param callbackInterface the callback interface to implement.
     * @param listenerClass     the class that the static listener method belongs to.
     * @param listener          the weak reference to the listener object which holds the method that will implement
     *                          the callback interface.
     * @param listenerMethod    the reference to the method that will implement the callback interface.
     * @param defaultImpl       the default implementation called by the interface instance if the referenced object
     *                          has been garbage-collected before this instance has been unregistered.
     * @param settings          additional settings for how the interface instance should be implemented.
     * @param <T>               the type of callback interface returned.
     * @return the newly created callback interface.
     * @throws BadListenerException if the listener method or callback interface make it impossible to create a valid
     *                              implementation.
     */
    public static <T> T buildWeakListener(Class<T> callbackInterface, Class<?> listenerClass, WeakReference<?> listener,
                                          Method listenerMethod, @Nullable T defaultImpl,
                                          BuilderSettings settings) throws BadListenerException {
        Methods methods = verifyMethods(listenerClass, listenerMethod, callbackInterface, settings);

        try {
            return WeakReferenceListenerGenerator.defineWrapper(callbackInterface, listenerClass, listenerMethod,
                listener, defaultImpl);
        } catch (Throwable t) {
            throw handleError(callbackInterface, listenerClass, listenerMethod, methods, t);
        }
    }

    private static @NotNull Methods verifyMethods(Class<?> listenerClass, Method listenerMethod,
                                                  Class<?> callbackInterface, BuilderSettings settings)
        throws BadListenerException {
        Method interfaceMethod = AdapterUtils.getSingularMethod(callbackInterface);
        if (interfaceMethod == null) throw new BadListenerException(
            "Tried to listen to callback interface " + callbackInterface + " which is not a functional interface");

        MethodType expectedMethodType =
            MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes());
        MethodType actualMethodType =
            MethodType.methodType(listenerMethod.getReturnType(), listenerMethod.getParameterTypes());
        Methods methods = new Methods(interfaceMethod, expectedMethodType, actualMethodType);

        checkReturnTypes(callbackInterface, listenerClass, listenerMethod, methods);
        checkArgumentTypes(callbackInterface, listenerClass, listenerMethod, methods, settings);
        return methods;
    }

    private static void checkReturnTypes(Class<?> callbackInterface, Class<?> listenerClass, Method listenerMethod,
                                         Methods methods) throws BadListenerException {
        if (listenerMethod.getReturnType().equals(void.class) &&
            !methods.interfaceMethod().getReturnType().equals(void.class)) {
            throw new BadListenerException(
                "Listener method " + toString(listenerClass, listenerMethod.getName(), methods.actualMethodType()) +
                    " has incompatible return type with callback interface method " +
                    toString(callbackInterface, methods.interfaceMethod().getName(), methods.expectedMethodType()));
        }

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

    private static void checkArgumentTypes(Class<?> callbackInterface, Class<?> listenerClass, Method listenerMethod,
                                           Methods methods, BuilderSettings settings) throws BadListenerException {
        Class<?>[] listenerParams = listenerMethod.getParameterTypes();
        Class<?>[] interfaceParams = methods.interfaceMethod().getParameterTypes();
        if (settings.requireAllArgs() ?
            (listenerParams.length != interfaceParams.length) :
            (listenerParams.length > interfaceParams.length)) {
            throw new BadListenerException(
                "Listener method " + toString(listenerClass, listenerMethod.getName(), methods.actualMethodType()) +
                    " (" + listenerMethod.getParameterCount() + " arguments)" +
                    " has incompatible arguments with callback interface method " +
                    toString(callbackInterface, methods.interfaceMethod().getName(), methods.expectedMethodType()) +
                    " (" + methods.interfaceMethod().getParameterCount() + " arguments)");
        }

        for (int i = 0; i < listenerParams.length; i++) {
            if (!listenerParams[i].isAssignableFrom(interfaceParams[i])) {
                throw new BadListenerException(
                    "Listener method " + toString(listenerClass, listenerMethod.getName(), methods.actualMethodType()) +
                        " has incompatible arguments with callback interface method " +
                        toString(callbackInterface, methods.interfaceMethod().getName(), methods.expectedMethodType()) +
                        ". Offending listener method parameter #" + i + " " + listenerParams[i] +
                        " is not assignable from interface method parameter " + interfaceParams[i]);
            }
        }
    }

    private static BadListenerException handleError(Class<?> callbackInterface, Class<?> listenerClass,
                                                    Method listenerMethod, Methods methods, Throwable t) {
        return new BadListenerException("Error connecting listener method " +
            toString(listenerClass, listenerMethod.getName(), methods.actualMethodType()) +
            " with callback interface " +
            toString(callbackInterface, methods.interfaceMethod().getName(), methods.expectedMethodType()), t);
    }

    private static String toString(Class<?> clazz, String name, MethodType methodType) {
        String className = clazz.getName().replace('.', '/');
        Type type = AdapterUtils.getMethodType(methodType);
        return className + "." + name + type;
    }

    private record Methods(Method interfaceMethod, MethodType expectedMethodType, MethodType actualMethodType) {
    }
}
