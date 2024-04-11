package com.kneelawk.commonevents.impl;

import net.minecraft.resources.ResourceLocation;

public class CommonEventsImpl {
    public static final String MOD_ID = "common_events";

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
