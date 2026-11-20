package com.busdedsafio.client;

import java.util.function.Predicate;

import com.busdedsafio.BusEntity;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Sonido en bucle pegado al bus. Se detiene cuando la condicion deja de cumplirse. */
public class BusSound extends AbstractTickableSoundInstance {
	private final BusEntity bus;
	private final Predicate<BusEntity> condition;

	public BusSound(BusEntity bus, SoundEvent event, float volume, Predicate<BusEntity> condition) {
		super(event, SoundSource.NEUTRAL, RandomSource.create());
		this.bus = bus;
		this.condition = condition;
		this.looping = true;
		this.delay = 0;
		this.volume = volume;
		this.x = bus.getX();
		this.y = bus.getY();
		this.z = bus.getZ();
	}

	@Override
	public void tick() {
		if (bus.isRemoved() || !condition.test(bus)) {
			this.stop();
			return;
		}
		this.x = bus.getX();
		this.y = bus.getY() + 1.5;
		this.z = bus.getZ();
	}
}
