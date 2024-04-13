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

package com.kneelawk.commonevents.impl.scan;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.impl.CEConstants;
import com.kneelawk.commonevents.impl.CELog;

public class ClassScanner extends ClassVisitor {
    private static final int API = Opcodes.ASM9;
    public static final String LISTENER_ANNOTATION_NAME = "Lcom/kneelawk/commonevents/api/Listener;";
    public static final String LISTEN_ANNOTATION_NAME = "Lcom/kneelawk/commonevents/api/Listen;";

    public static void scan(URL classUrl, List<String> modIds, boolean isClientSide,
                            Consumer<ListenerHandle> listenerFound) {
        try (InputStream is = classUrl.openStream(); BufferedInputStream buffered = new BufferedInputStream(is)) {
            scan(buffered, isClientSide, listenerFound);
        } catch (IOException e) {
            CELog.LOGGER.warn("[Common Events] Error scanning class {} in mod {}", classUrl, modIds, e);
        }
    }

    public static void scan(Path classPath, List<String> modIds, boolean isClientSide,
                            Consumer<ListenerHandle> listenerFound) {
        try (InputStream is = Files.newInputStream(classPath);
             BufferedInputStream buffered = new BufferedInputStream(is)) {
            scan(buffered, isClientSide, listenerFound);
        } catch (IOException e) {
            CELog.LOGGER.warn("[Common Events] Error scanning class {} in mod {}", classPath, modIds, e);
        }
    }

    private static void scan(BufferedInputStream buffered, boolean isClientSide, Consumer<ListenerHandle> listenerFound)
        throws IOException {
        ClassReader cr = new ClassReader(buffered);
        cr.accept(new ClassScanner(isClientSide, listenerFound),
            ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    private final boolean isClientSide;
    private final Consumer<ListenerHandle> listenerFound;
    private Type visitingClass = null;
    private boolean shouldScan = false;

    protected ClassScanner(boolean isClientSide, Consumer<ListenerHandle> listenerFound) {
        super(API);
        this.isClientSide = isClientSide;
        this.listenerFound = listenerFound;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        visitingClass = Type.getObjectType(name);
        System.out.println("Visiting class: " + name);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        System.out.println("Visiting annotation: " + descriptor);
        if (LISTENER_ANNOTATION_NAME.equals(descriptor)) {
            CELog.LOGGER.debug("[Common Events] Found annotated class: {}", descriptor);
            return new ClassAnnotationScanner();
        } else {
            return null;
        }
    }

    private class ClassAnnotationScanner extends AnnotationVisitor {
        private boolean isValidSide = true;

        protected ClassAnnotationScanner() {
            super(API);
        }

        @Override
        public void visit(String name, Object value) {
            System.out.println("Visiting component: " + name + " : " + value);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            System.out.println("Visiting enum: " + name + " : " + descriptor + " : " + value);

            if ("side".equals(name)) {
                isValidSide = "BOTH".equals(value) || ("CLIENT".equals(value) == isClientSide);
            }
        }

        @Override
        public void visitEnd() {
            if (isValidSide) {
                shouldScan = true;
            } else {
                CELog.LOGGER.debug("[Common Events] Skipping {} because it is on the wrong side.", visitingClass);
            }
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        if (!shouldScan) return null;

        System.out.println("Visiting method: " + name + " : " + descriptor);

        if ((access & Opcodes.ACC_PUBLIC) != 0 && ((access & Opcodes.ACC_STATIC) != 0)) {
            return new MethodScanner(name, Type.getMethodType(descriptor));
        } else {
            return null;
        }
    }

    private class MethodScanner extends MethodVisitor {
        private final String name;
        private final Type descriptor;

        protected MethodScanner(String name, Type descriptor) {
            super(API);
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            System.out.println("Visiting method annotation: " + descriptor);

            if (LISTEN_ANNOTATION_NAME.equals(descriptor)) {
                return new MethodAnnotationScanner();
            } else {
                return null;
            }
        }

        private class MethodAnnotationScanner extends AnnotationVisitor {
            private ListenerKey key = null;
            private ResourceLocation phase = CEConstants.DEFAULT_PHASE;

            protected MethodAnnotationScanner() {
                super(API);
            }

            @Override
            public void visit(String name, Object value) {
                System.out.println("Visiting component: " + name + " : " + value + " (" + value.getClass() + ")");

                if ("value".equals(name) && value instanceof Type type) {
                    key = new ListenerKey(type);
                } else if ("phase".equals(name) && value instanceof String str) {
                    try {
                        phase = new ResourceLocation(str);
                    } catch (ResourceLocationException e) {
                        CELog.LOGGER.warn("[Common Events] Encountered invalid phase '{}' in {}.{}{}", str,
                            visitingClass, name, descriptor, e);
                    }
                }
            }

            @Override
            public void visitEnd() {
                if (key != null) {
                    listenerFound.accept(new SimpleListenerHandle(key, phase, visitingClass, name, descriptor));
                }
            }
        }
    }
}
