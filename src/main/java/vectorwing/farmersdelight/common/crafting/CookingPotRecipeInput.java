package vectorwing.farmersdelight.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.items.IItemHandler;

public record CookingPotRecipeInput(ItemStack[] items) implements RecipeInput {
    public static CookingPotRecipeInput from(IItemHandler inventory) {
        ItemStack[] items = new ItemStack[8];
        for (int i = 0; i < items.length; i++) items[i] = inventory.getStackInSlot(i);
        return new CookingPotRecipeInput(items);
    }
    @Override public ItemStack getItem(int index) { return items[index]; }
    @Override public int size() { return items.length; }
}
