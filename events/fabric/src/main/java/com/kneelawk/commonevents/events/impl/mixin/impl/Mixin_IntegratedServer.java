package com.kneelawk.commonevents.events.impl.mixin.impl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;

import com.kneelawk.commonevents.events.api.lifecycle.ServerLifecycleEvents;

@Mixin({IntegratedServer.class})
public class Mixin_IntegratedServer {
    @Inject(method = "initServer", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/server/IntegratedServer;loadLevel()V"))
    private void onLoadingWorlds(CallbackInfoReturnable<Boolean> cir) {
        ServerLifecycleEvents.SERVER_LOADING_WORLDS.invoker().onWorldsLoading((MinecraftServer) (Object) this);
    }
}
