package com.kneelawk.commonevents.test.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kneelawk.commonevents.api.adapter.mod.ModFileHolder;

public class TestDirModFileHolder implements ModFileHolder {
    private final Path resourcePath;
    private final List<Path> rootPaths = new ArrayList<>();
    private final String name;

    public TestDirModFileHolder(Path resourcePath) {
        this.resourcePath = resourcePath;
        rootPaths.add(resourcePath);
        name = "[" + resourcePath + "]";
    }

    public Path getResourcePath() {
        return resourcePath;
    }

    public void addRootPath(Path newRootPath) {
        rootPaths.add(newRootPath);
    }

    @Override
    public @NotNull String getModIdStr() {
        return name;
    }

    @Override
    public @Nullable Path getResource(@NotNull String path) {
        for (Path root : rootPaths) {
            Path sub = root.resolve(path);
            if (Files.exists(sub)) {
                return sub;
            }
        }
        return rootPaths.getFirst().resolve(path);
    }

    @Override
    public @NotNull List<Path> getRootPaths() {
        return rootPaths;
    }
}
