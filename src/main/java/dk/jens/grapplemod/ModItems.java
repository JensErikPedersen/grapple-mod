package dk.jens.grapplemod;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

public class ModItems {
	// Gribekrogen. Man kan kun have én pr. plads i inventaret.
	public static final Item GRAPPLING_HOOK = register("grappling_hook", GrapplingHookItem::new,
			new Item.Properties().stacksTo(1));

	private static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties properties) {
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(GrappleMod.MOD_ID, name));
		Item item = itemFactory.apply(properties.setId(itemKey));
		return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
	}

	public static void initialize() {
		// Læg gribekrogen i fanen "Værktøj og hjælpemidler" i kreativ tilstand
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(creativeTab -> {
			creativeTab.accept(GRAPPLING_HOOK);
		});
	}
}
