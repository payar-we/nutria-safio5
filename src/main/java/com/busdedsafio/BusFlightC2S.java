package com.busdedsafio;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** El conductor pide activar o desactivar el vuelo (doble salto). */
public record BusFlightC2S(boolean flying) implements CustomPacketPayload {
	public static final Type<BusFlightC2S> TYPE = new Type<>(BusMod.id("flight"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BusFlightC2S> CODEC =
			StreamCodec.composite(ByteBufCodecs.BOOL, BusFlightC2S::flying, BusFlightC2S::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
