package vectorwing.farmersdelight.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.concurrent.CompletableFuture;

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

                /*
                 * In Minecraft 26.1, an Item's default data components are bound to its registry holder
                 * during resource reload (see ReloadableServerResources), which never happens in a datagen
                 * run. Any ItemStack created by a provider (recipe builders, advancement icons...) crashes
                 * with "Components not bound yet" unless the pending component maps are applied first.
                 * This chains the binding onto the registry future, so every provider below sees fully
                 * bound items by the time it runs.
                 *
                 * NeoForge's dev-only component validation (CommonHooks.validateComponent) rejects the
                 * lazy tag-backed HolderSets used by e.g. Repairable components, because tag contents
                 * are never loaded in a datagen run. That validation is skipped in production and has
                 * no effect on generated output, so it is suspended only for the duration of the
                 * binding and restored immediately afterwards.
                 */
                CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries = event.getLookupProvider()
                                .thenApply(provider -> {
                                        boolean wasRunningInIde = net.minecraft.SharedConstants.IS_RUNNING_IN_IDE;
                                        net.minecraft.SharedConstants.IS_RUNNING_IN_IDE = false;
                                        try {
                                                BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(provider)
                                                                .forEach(DataComponentInitializers.PendingComponents::apply);
                                        } finally {
                                                net.minecraft.SharedConstants.IS_RUNNING_IN_IDE = wasRunningInIde;
                                        }
                                        return provider;
                                });

                BlockTags blockTags = new BlockTags(output, registries);
                generator.addProvider(true, blockTags);
                generator.addProvider(true, new ItemTags(output, registries, blockTags.contentsGetter()));
                generator.addProvider(true, new EntityTags(output, registries));
                generator.addProvider(true, new DamageTypeTags(output, registries));
                generator.addProvider(true, new EnchantmentTags(output, registries));
                generator.addProvider(true, new Recipes.Runner(output, registries));
                generator.addProvider(true, new LootModifiers(output, registries));
                generator.addProvider(true, new DataMaps(output, registries));
                generator.addProvider(true, new Advancements(output, registries));
                generator.addProvider(true, new LootTables(output, registries));
                generator.addProvider(true, new SoundDefinitions(output));
                generator.addProvider(true, new VillagerTradeProvider(output));
                generator.addProvider(true, new BlockStates(output));
                generator.addProvider(true, new ItemModels(output));
        }
}
