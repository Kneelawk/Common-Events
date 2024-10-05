package com.kneelawk.commonevents.events.impl.mixin.impl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;

import com.kneelawk.commonevents.events.api.lifecycle.ServerLifecycleEvents;

@Mixin(MinecraftServer.class)
public class Mixin_MinecraftServer {
    @Inject(method = "runServer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/MinecraftServer;onServerExit()V"))
    private void onServerStopped(CallbackInfo ci) {
        ServerLifecycleEvents.SERVER_STOPPED.invoker().onServerStopped((MinecraftServer) (Object) this);
    }
}
