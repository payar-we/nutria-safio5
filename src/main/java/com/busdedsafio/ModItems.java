package com.busdedsafio;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModItems {
	public static final Item BUS_ITEM = new BusItem(new Item.Properties().stacksTo(1));
	/** Ticket rojo = asiento de adelante (conductor). */
	public static final Item TICKET_ROJO = new Item(new Item.Properties().stacksTo(1));
	/** Ticket amarillo = silla de atras (6 personas). */
	public static final Item TICKET_AMARILLO = new Item(new Item.Properties().stacksTo(1));

	public static final CreativeModeTab TAB = FabricItemGroup.builder()
			.title(Component.translatable("itemGroup.busdedsafio.bus"))
			.icon(() -> new ItemStack(BUS_ITEM))
			.displayItems((params, out) -> {
				out.accept(BUS_ITEM);
				out.accept(TICKET_ROJO);
				out.accept(TICKET_AMARILLO);
			})
			.build();

	public static void register() {
		Registry.register(BuiltInRegistries.ITEM, BusMod.id("bus"), BUS_ITEM);
		Registry.register(BuiltInRegistries.ITEM, BusMod.id("ticket_rojo"), TICKET_ROJO);
		Registry.register(BuiltInRegistries.ITEM, BusMod.id("ticket_amarillo"), TICKET_AMARILLO);
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BusMod.id("bus"), TAB);
	}
}
