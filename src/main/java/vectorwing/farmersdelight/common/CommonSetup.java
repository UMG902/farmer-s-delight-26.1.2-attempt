package vectorwing.farmersdelight.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.accessor.VillagerProfessionAccessor;

import java.util.HashMap;

public class CommonSetup
{
	public static void init(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			registerDispenserBehaviors();
			registerItemSetAdditions();
			registerRichSoilFarmlandAsFarmerPoi();
		});
	}

	/** Adds Rich Soil Farmland to the vanilla farmer profession after all mod registries are bound. */
	private static void registerRichSoilFarmlandAsFarmerPoi() {
		Block richSoilFarmland = ModBlocks.RICH_SOIL_FARMLAND.get();
		for (VillagerProfession profession : BuiltInRegistries.VILLAGER_PROFESSION) {
			Identifier key = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession);
			if (key != null && "minecraft".equals(key.getNamespace()) && "farmer".equals(key.getPath())) {
				VillagerProfessionAccessor accessor = (VillagerProfessionAccessor) (Object) profession;
				ImmutableSet<Block> secondaryPoi = accessor.farmersdelight$getSecondaryPoi();
				if (secondaryPoi.contains(Blocks.FARMLAND) && !secondaryPoi.contains(richSoilFarmland)) {
					accessor.farmersdelight$setSecondaryPoi(ImmutableSet.<Block>builder()
							.addAll(secondaryPoi)
							.add(richSoilFarmland)
							.build());
				}
				return;
			}
		}
	}

	public static void registerDispenserBehaviors() {
		DispenserBlock.registerProjectileBehavior(ModItems.ROTTEN_TOMATO.get());
	}

	public static void registerItemSetAdditions() {
		HashMap<Item, Integer> newFoodPoints = new HashMap<>();
		newFoodPoints.put(ModItems.CABBAGE.get(), 1);
		newFoodPoints.put(ModItems.TOMATO.get(), 1);
		newFoodPoints.put(ModItems.ONION.get(), 1);
		newFoodPoints.put(ModItems.RICE.get(), 2);
		newFoodPoints.putAll(Villager.FOOD_POINTS);

		Villager.FOOD_POINTS = ImmutableMap.copyOf(newFoodPoints);
	}
}
