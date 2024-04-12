package com.kneelawk.commonevents.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;

public class CommonEventsImpl {
    public static final String MOD_ID = "common_events";

    public static Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
