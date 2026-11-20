package com.busdedsafio.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.busdedsafio.BusEntity;
import com.busdedsafio.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

public class BusSounds {
	private static final Map<Integer, BusSound> MUSIC = new HashMap<>();
	private static final Map<Integer, BusSound> ROTOR = new HashMap<>();

	public static void tick(Minecraft mc) {
		if (mc.level == null) {
			MUSIC.clear();
			ROTOR.clear();
			return;
		}
		for (Entity e : mc.level.entitiesForRendering()) {
			if (e instanceof BusEntity bus && !bus.isRemoved()) {
				// musica mientras haya conductor; helices mientras vuela
				ensure(mc, MUSIC, bus, ModSounds.BUS_MUSIC, 0.8F, b -> b.getControllingPassenger() != null);
				ensure(mc, ROTOR, bus, ModSounds.BUS_ROTOR, 1.0F, BusEntity::isFlying);
			}
		}
	}

	private static void ensure(Minecraft mc, Map<Integer, BusSound> map, BusEntity bus, SoundEvent event, float volume, Predicate<BusEntity> cond) {
		BusSound current = map.get(bus.getId());
		if (current != null && !current.isStopped()) return;
		if (cond.test(bus)) {
			BusSound s = new BusSound(bus, event, volume, cond);
			map.put(bus.getId(), s);
			mc.getSoundManager().play(s);
		} else {
			map.remove(bus.getId());
		}
	}
}
