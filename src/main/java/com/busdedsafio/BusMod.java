package com.busdedsafio;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class BusMod implements ModInitializer {
	public static final String MOD_ID = "busdedsafio";

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModSounds.register();
		ModEntities.register();
		ModItems.register();

		PayloadTypeRegistry.playC2S().register(BusFlightC2S.TYPE, BusFlightC2S.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(BusFlightC2S.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			if (player.getVehicle() instanceof BusEntity bus && bus.getControllingPassenger() == player) {
				bus.setFlying(payload.flying());
			}
		});
	}
}
