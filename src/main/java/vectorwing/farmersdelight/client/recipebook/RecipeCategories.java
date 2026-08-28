package vectorwing.farmersdelight.client.recipebook;

import net.neoforged.neoforge.client.event.RegisterRecipeBookSearchCategoriesEvent;

public final class RecipeCategories {
    private RecipeCategories() {}
    public static void init(RegisterRecipeBookSearchCategoriesEvent event) {
        // Custom recipe-book categories require the 26.1 registry/search-category API.
        // The cooking recipes are currently marked as not placeable/special, so no registration is needed here.
    }
}
