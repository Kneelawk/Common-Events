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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import com.kneelawk.commonevents.impl.CELog;

public class ClassScanner extends ClassVisitor {
    public static void scan(URL classUrl, List<String> modIds) {
        try (InputStream is = classUrl.openStream(); BufferedInputStream buffered = new BufferedInputStream(is)) {
            scan(buffered);
        } catch (IOException e) {
            CELog.LOGGER.warn("[Common Events] Error scanning class {} in mod {}", classUrl, modIds, e);
        }
    }

    public static void scan(Path classPath, List<String> modIds) {
        try (InputStream is = Files.newInputStream(classPath);
             BufferedInputStream buffered = new BufferedInputStream(is)) {
            scan(buffered);
        } catch (IOException e) {
            CELog.LOGGER.warn("[Common Events] Error scanning class {} in mod {}", classPath, modIds, e);
        }
    }

    private static void scan(BufferedInputStream buffered) throws IOException {
        ClassReader cr = new ClassReader(buffered);
        cr.accept(new ClassScanner(), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    protected ClassScanner() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println("Visiting " + name);
    }
}
