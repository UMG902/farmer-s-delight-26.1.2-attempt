package vectorwing.farmersdelight.data;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.Optional;

/**
 * Datagen helpers for tag-based ingredients.
 *
 * In a datagen run the item registry only knows tags that ship with vanilla and NeoForge;
 * tags the mod itself generates (e.g. c:drinks/milk) are not bound yet. Referencing such a
 * tag with {@code BuiltInRegistries.ITEM.getOrThrow(tag)} crashes the run, even though the
 * ingredient only ever encodes the tag key (#c:drinks/milk), never its contents.
 * This helper falls back to an unbound named holder set so any tag key can be referenced.
 */
public class DataTags
{
	public static Ingredient tagIngredient(TagKey<Item> tag) {
		HolderSet<Item> holders = BuiltInRegistries.ITEM.get(tag)
				.orElseGet(() -> HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag));
		return Ingredient.of(holders);
	}


	/**
	 * An item lookup that resolves tags against the live item registry and falls back to an
	 * unbound named holder set for tags this mod generates (which are not bound in datagen).
	 */
	public static final HolderGetter<Item> TOLERANT_ITEM_LOOKUP = new HolderGetter<>() {
		@Override
		public Optional<Holder.Reference<Item>> get(ResourceKey<Item> key) {
			return BuiltInRegistries.ITEM.get(key);
		}

		@Override
		public Optional<HolderSet.Named<Item>> get(TagKey<Item> tag) {
			return BuiltInRegistries.ITEM.get(tag)
					.or(() -> Optional.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag)));
		}
	};

	/** A tool-matching predicate for a tag, tolerant of tags that are not bound during datagen. */
	public static LootItemCondition.Builder toolMatches(TagKey<Item> tag) {
		return MatchTool.toolMatches(ItemPredicate.Builder.item().of(TOLERANT_ITEM_LOOKUP, tag));
	}

	/** Same tolerant lookup, for the block registry (used by location block predicates). */
	public static final HolderGetter<Block> TOLERANT_BLOCK_LOOKUP = new HolderGetter<>() {
		@Override
		public Optional<Holder.Reference<Block>> get(ResourceKey<Block> key) {
			return BuiltInRegistries.BLOCK.get(key);
		}

		@Override
		public Optional<HolderSet.Named<Block>> get(TagKey<Block> tag) {
			return BuiltInRegistries.BLOCK.get(tag)
					.or(() -> Optional.of(HolderSet.emptyNamed(BuiltInRegistries.BLOCK, tag)));
		}
	};
}
