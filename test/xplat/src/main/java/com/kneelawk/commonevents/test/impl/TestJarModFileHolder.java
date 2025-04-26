package com.kneelawk.commonevents.test.impl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kneelawk.commonevents.api.adapter.mod.ModFileHolder;

public class TestJarModFileHolder implements ModFileHolder {
    private final FileSystem jarFileSystem;
    private final String name;

    public TestJarModFileHolder(Path jarPath) {
        try {
            jarFileSystem = FileSystems.newFileSystem(jarPath);
        } catch (IOException e) {
            throw new RuntimeException("Error opening mod jar: " + jarPath, e);
        }
        name = "[" + jarPath.getFileName() + "]";
    }

    @Override
    public @NotNull String getModIdStr() {
        return name;
    }

    @Override
    public @Nullable Path getResource(String path) {
        return jarFileSystem.getPath(path);
    }

    @Override
    public @NotNull List<Path> getRootPaths() {
        return ImmutableList.copyOf(jarFileSystem.getRootDirectories());
    }
}
