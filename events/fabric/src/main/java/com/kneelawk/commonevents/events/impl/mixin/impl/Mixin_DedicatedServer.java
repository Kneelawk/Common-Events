package com.kneelawk.commonevents.events.impl.mixin.impl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

import com.kneelawk.commonevents.events.api.lifecycle.ServerLifecycleEvents;

@Mixin(DedicatedServer.class)
public class Mixin_DedicatedServer {
    @Inject(method = "initServer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/dedicated/DedicatedServer;getLevelIdName()Ljava/lang/String;"))
    private void onLoadingWorlds(CallbackInfoReturnable<Boolean> cir) {
        ServerLifecycleEvents.SERVER_LOADING_WORLDS.invoker().onWorldsLoading((MinecraftServer) (Object) this);
    }
}
