package com.kneelawk.commonevents.impl.gen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.objectweb.asm.Type;

import com.kneelawk.commonevents.impl.CEConstants;
import com.kneelawk.commonevents.impl.CELog;
import com.kneelawk.commonevents.impl.Platform;

/**
 * Abstract helper super-class for code-generator classes.
 *
 * @param <S> the spec type the code generator uses to define unique generated classes.
 */
public abstract class AbstractCodeGenerator<S> {
    private static final Path EXPORT_GENERATED_PATH =
        Platform.getInstance().getGameDirectory().resolve(".common-events");

    private class Loader extends ClassLoader {
        public Loader(String name, ClassLoader parent) {
            super(name, parent);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            S spec = classToKey.get(name);
            if (spec == null) throw new ClassNotFoundException(name);

            String internalName = name.replace('.', '/');
            byte[] bytes = generateClass(spec, Type.getObjectType(internalName));

            if (CEConstants.EXPORT_GENERATED_CLASSES) {
                CELog.LOGGER.info("[Common Events] Exporting generated class {}", name);

                Path classPath = EXPORT_GENERATED_PATH.resolve(internalName + ".class");
                Path classInfoPath = EXPORT_GENERATED_PATH.resolve(internalName + ".info");

                try {
                    Path parentPath = classPath.getParent();
                    if (!Files.exists(parentPath)) {
                        Files.createDirectories(parentPath);
                    }

                    Files.write(classPath, bytes);
                    Files.writeString(classInfoPath, getInfo(spec));
                } catch (IOException e) {
                    CELog.LOGGER.warn("[Common Events] Unable to write exported generated class to {}", classPath, e);
                }
            }

            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<S, String> keyToClass = new HashMap<>();
    private final Map<String, S> classToKey = new HashMap<>();
    private final AtomicInteger index = new AtomicInteger(0);
    private final String prefix;

    private final Loader loader;

    /**
     * Initializes the code generator.
     *
     * @param prefix the path prefix used for all generated classes.
     * @param name   the name of the generating class loader.
     */
    protected AbstractCodeGenerator(String prefix, String name) {
        this.prefix = prefix;

        loader = new Loader(name, getClass().getClassLoader());
    }

    /**
     * Generates and loads a class based on the implementation of {@link #generateClass(Object, Type)}.
     *
     * @param spec the spec used to generate the class.
     * @return the loaded generated class.
     * @throws ClassNotFoundException if there was an error creating the class.
     */
    protected Class<?> getOrCreateClass(S spec) throws ClassNotFoundException {
        String className;
        lock.readLock().lock();
        try {
            className = keyToClass.get(spec);
        } finally {
            lock.readLock().unlock();
        }

        if (className == null) {
            lock.writeLock().lock();
            try {
                className = keyToClass.get(spec);

                if (className == null) {
                    className = prefix + "$" + index.getAndIncrement();
                    keyToClass.put(spec, className);
                    classToKey.put(className, spec);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        return loader.loadClass(className);
    }

    /**
     * Implementors should use this to generate the actual code of the class.
     *
     * @param spec         the spec used to define the class.
     * @param beingDefined the type of the class being defined.
     * @return the bytecode for the class being defined.
     */
    protected abstract byte[] generateClass(S spec, Type beingDefined);

    /**
     * Implementors should use this to provide a custom string representation of a spec for use in debug exporting of
     * generated classes.
     *
     * @param spec the spec used to define the class this debug info is associated with.
     * @return the string representation of the spec.
     */
    protected String getInfo(S spec) {
        return spec.toString();
    }
}
