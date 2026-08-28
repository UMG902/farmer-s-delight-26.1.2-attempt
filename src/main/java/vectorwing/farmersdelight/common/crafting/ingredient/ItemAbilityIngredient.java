package vectorwing.farmersdelight.common.crafting.ingredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.ItemAbility;

/** In-code bridge used by recipe generation. */
public class ItemAbilityIngredient {
    private final ItemAbility itemAbility;
    public ItemAbilityIngredient(ItemAbility itemAbility) { this.itemAbility = itemAbility; }
    public Ingredient toVanilla() {
        return Ingredient.of(BuiltInRegistries.ITEM.stream()
                .filter(item -> new ItemStack(item).canPerformAction(itemAbility))
                .map(Item::asItem));
    }
    public ItemAbility getItemAbility() { return itemAbility; }
}
