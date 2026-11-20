package com.busdedsafio;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Clic derecho en el suelo para poner el bus. */
public class BusItem extends Item {
	public BusItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
		BusEntity bus = new BusEntity(ModEntities.BUS, level);
		bus.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, context.getRotation(), 0.0F);
		level.addFreshEntity(bus);
		context.getItemInHand().consume(1, context.getPlayer());
		return InteractionResult.CONSUME;
	}
}
