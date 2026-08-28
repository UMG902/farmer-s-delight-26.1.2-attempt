package vectorwing.farmersdelight.integration.jei;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.utility.RecipeUtils;

import java.util.Collection;
import java.util.List;

public class FDRecipes
{
	private static RecipeMap recipeManager = RecipeMap.EMPTY;

	public FDRecipes() {
		// The client recipe map is populated by RecipeSyncEvents when the server sends
		// the datapack recipe contents. Do not try to obtain it through RecipeAccess:
		// that API no longer exposes a RecipeMap in Minecraft 26.1.2.
	}

	public Collection<RecipeHolder<CookingPotRecipe>> getCookingPotRecipes() {
		return recipeManager.byType(ModRecipeTypes.COOKING.get());
	}

	public Collection<RecipeHolder<CuttingBoardRecipe>> getCuttingBoardRecipes() {
		return recipeManager.byType(ModRecipeTypes.CUTTING.get());
	}

	public static void setRecipeMap(RecipeMap recipes) {
		recipeManager = recipes;
	}

	public List<RecipeHolder<CraftingRecipe>> getSpecialCraftingRecipes() {
		List<RecipeHolder<CraftingRecipe>> recipes = Lists.newArrayList();

		addValidatedSpecialRecipe(recipes, "wheat_dough_from_water", "fd_dough",
				NonNullList.of(
						Ingredient.of(Items.WHEAT),
						Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.BUCKETS_WATER))
				),
				ModItems.WHEAT_DOUGH.get()
		);

		return recipes;
	}

	public void addValidatedSpecialRecipe(List<RecipeHolder<CraftingRecipe>> recipeList, String recipeId, String group, NonNullList<Ingredient> inputs, ItemLike output) {
		RecipeHolder<?> recipe = recipeManager.byKey(RecipeUtils.FDKey(recipeId));
		if (recipe != null) {
			// JEI needs a CraftingRecipe-shaped display object for custom/special recipes.
			// The real server-side recipe remains the registered custom recipe.
			Recipe.CommonInfo commonInfo = new Recipe.CommonInfo(false);
			CraftingRecipe.CraftingBookInfo bookInfo =
					new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, group);
			ItemStackTemplate result = new ItemStackTemplate(output.asItem());

			recipeList.add(new RecipeHolder<>(
				recipe.id(),
				new ShapelessRecipe(commonInfo, bookInfo, result, inputs)
			));
		}
	}

}
