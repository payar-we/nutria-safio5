package com.busdedsafio;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
	public static final SoundEvent BUS_MUSIC = make("bus_music");
	public static final SoundEvent BUS_ROTOR = make("bus_rotor");

	private static SoundEvent make(String name) {
		ResourceLocation id = BusMod.id(name);
		return SoundEvent.createVariableRangeEvent(id);
	}

	public static void register() {
		Registry.register(BuiltInRegistries.SOUND_EVENT, BusMod.id("bus_music"), BUS_MUSIC);
		Registry.register(BuiltInRegistries.SOUND_EVENT, BusMod.id("bus_rotor"), BUS_ROTOR);
	}
}
