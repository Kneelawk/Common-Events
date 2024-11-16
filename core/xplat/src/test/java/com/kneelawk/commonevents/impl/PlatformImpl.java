package com.kneelawk.commonevents.impl;

import java.nio.file.Path;
import java.util.List;

import com.kneelawk.commonevents.api.adapter.mod.ModFileHolder;

public class PlatformImpl extends Platform {
    @Override
    public boolean isPhysicalClient() {
        return true;
    }

    @Override
    public String getModVersion() {
        return "test";
    }

    @Override
    public List<? extends ModFileHolder> getModFiles() {
        return List.of();
    }

    @Override
    public Path getGameDirectory() {
        return Path.of("").toAbsolutePath();
    }
}
