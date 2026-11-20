package com.busdedsafio;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
	public static final EntityType<BusEntity> BUS = EntityType.Builder
			.<BusEntity>of(BusEntity::new, MobCategory.MISC)
			.sized(3.0F, 2.9F)
			.clientTrackingRange(10)
			.updateInterval(1)
			.fireImmune()
			.build("bus");

	public static void register() {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, BusMod.id("bus"), BUS);
	}
}
