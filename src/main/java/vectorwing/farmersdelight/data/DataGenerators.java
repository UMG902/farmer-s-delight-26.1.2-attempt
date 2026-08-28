package vectorwing.farmersdelight.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModBiomeModifiers;
import vectorwing.farmersdelight.common.registry.ModDamageTypes;
import vectorwing.farmersdelight.common.world.WildCropGeneration;
import vectorwing.farmersdelight.data.provider.LootTables;

@EventBusSubscriber(modid = FarmersDelight.MODID)
public class DataGenerators
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent.Client event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();

		RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder()
				.add(Registries.CONFIGURED_FEATURE, WildCropGeneration::bootstrapConfiguredFeatures)
				.add(Registries.PLACED_FEATURE, WildCropGeneration::bootstrapPlacedFeatures)
				.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrapBiomeModifiers)
				.add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrapDamageTypes)
				.add(Registries.ENCHANTMENT, ModEnchantments::bootstrap);
		event.createDatapackRegistryObjects(registrySetBuilder);

		event.createBlockAndItemTags(BlockTags::new, ItemTags::new);
		event.createProvider(EntityTags::new);
		event.createProvider(DamageTypeTags::new);
		event.createProvider(EnchantmentTags::new);
		event.createProvider(Recipes.Runner::new);
		event.createProvider(LootModifiers::new);
		event.createProvider(DataMaps::new);
		event.createProvider(Advancements::new);
		event.createProvider(LootTables::new);
		event.createProvider(SoundDefinitions::new);

		// Stubbed: old model/blockstate datagen API no longer exists
		BlockStates blockStates = new BlockStates(output, null);
		generator.addProvider(true, blockStates);
		generator.addProvider(true, new ItemModels(output, null));
	}
}
