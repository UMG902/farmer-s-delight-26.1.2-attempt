package vectorwing.farmersdelight.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;

import java.util.Set;

/**
 * Stubbed - original used critereon imports and other API that changed.
 * Retains BlockLootSubProvider extension for LootTables compatibility.
 */
public class FDBlockLoot extends BlockLootSubProvider
{
	public FDBlockLoot(HolderLookup.Provider holder) {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags(), holder);
	}

	@Override
	protected void generate() {
		// Stubbed - original block loot tables need to be migrated to new API
	}
}
