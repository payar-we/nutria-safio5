package com.busdedsafio.client;

import com.busdedsafio.BusEntity;
import com.busdedsafio.BusFlightC2S;
import com.busdedsafio.BusInput;
import com.busdedsafio.ModEntities;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;

public class BusClient implements ClientModInitializer {
	private static int tickCount = 0;
	private static int lastJumpTick = -100;
	private static int flightCooldownUntil = 0;
	private static boolean jumpWasDown = false;

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.BUS, BusRenderer::new);
		ClientTickEvents.END_CLIENT_TICK.register(BusClient::tick);
	}

	private static void tick(Minecraft mc) {
		tickCount++;
		LocalPlayer player = mc.player;
		if (player != null && player.getVehicle() instanceof BusEntity bus && bus.getControllingPassenger() == player) {
			Options o = mc.options;
			BusInput.forward = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0);
			BusInput.side = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
			BusInput.up = o.keyJump.isDown();
			BusInput.down = o.keySprint.isDown();

			// doble salto = volar / dejar de volar
			boolean jump = o.keyJump.isDown();
			if (jump && !jumpWasDown) {
				if (tickCount - lastJumpTick <= 7) {
					ClientPlayNetworking.send(new BusFlightC2S(!bus.isFlying()));
					flightCooldownUntil = tickCount + 15;
					lastJumpTick = -100;
				} else {
					lastJumpTick = tickCount;
				}
			}
			jumpWasDown = jump;

			// al tocar el suelo volando, se quita el vuelo solo
			if (bus.isFlying() && bus.onGround() && tickCount > flightCooldownUntil) {
				ClientPlayNetworking.send(new BusFlightC2S(false));
				flightCooldownUntil = tickCount + 15;
			}
		} else {
			BusInput.reset();
			jumpWasDown = false;
		}
		BusSounds.tick(mc);
	}
}
