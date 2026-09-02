package vectorwing.farmersdelight.data.builder;

import vectorwing.farmersdelight.data.DataTags;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class CookingPotRecipeBuilder implements RecipeBuilder
{
	private CookingPotRecipeBookTab tab;
	private final NonNullList<Ingredient> ingredients = NonNullList.create();
	private final Item result;
	private final ItemStack resultStack;
	private final int cookingTime;
	private final float experience;
	private final ItemStackTemplate container;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
	@Nullable
	private String namespace;

	public CookingPotRecipeBuilder(ItemLike result, int count, int cookingTime, float experience, @Nullable ItemLike container) {
		this(new ItemStack(result, count), cookingTime, experience, container);
	}

	public CookingPotRecipeBuilder(ItemStack resultIn, int cookingTime, float experience, @Nullable ItemLike container) {
		this.result = resultIn.getItem();
		this.resultStack = resultIn;
		this.cookingTime = cookingTime;
		this.experience = experience;
		this.container = container != null ? ItemStackTemplate.fromNonEmptyStack(new ItemStack(container)) : null;
		this.tab = null;
	}

	public static CookingPotRecipeBuilder cookingPotRecipe(ItemLike mainResult, int count, int cookingTime, float experience) {
		return new CookingPotRecipeBuilder(mainResult, count, cookingTime, experience, null);
	}

	public static CookingPotRecipeBuilder cookingPotRecipe(ItemLike mainResult, int count, int cookingTime, float experience, ItemLike container) {
		return new CookingPotRecipeBuilder(mainResult, count, cookingTime, experience, container);
	}

	public CookingPotRecipeBuilder addIngredient(TagKey<Item> tagIn) {
		return addIngredient(DataTags.tagIngredient(tagIn));
	}

	public CookingPotRecipeBuilder addIngredient(ItemLike itemIn) {
		return addIngredient(itemIn, 1);
	}

	public CookingPotRecipeBuilder addIngredient(ItemLike itemIn, int quantity) {
		for (int i = 0; i < quantity; ++i) {
			addIngredient(Ingredient.of(itemIn));
		}
		return this;
	}

	public CookingPotRecipeBuilder addIngredient(Ingredient ingredientIn) {
		return addIngredient(ingredientIn, 1);
	}

	public CookingPotRecipeBuilder addIngredient(Ingredient ingredientIn, int quantity) {
		for (int i = 0; i < quantity; ++i) {
			ingredients.add(ingredientIn);
		}
		return this;
	}

	public CookingPotRecipeBuilder unlockedByAnyIngredient(ItemLike... items) {
		this.criteria.put("has_any_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(items));
		return this;
	}

	public RecipeBuilder group(@org.jetbrains.annotations.Nullable String p_176495_) {
		return this;
	}

	public CookingPotRecipeBuilder setRecipeBookTab(CookingPotRecipeBookTab tab) {
		this.tab = tab;
		return this;
	}

	public Item getResult() {
		return this.result;
	}

	@Override
	public CookingPotRecipeBuilder unlockedBy(String criterionName, Criterion<?> criterionTrigger) {
		this.criteria.put(criterionName, criterionTrigger);
		return this;
	}

	public CookingPotRecipeBuilder unlockedByItems(String criterionName, ItemLike... items) {
		return unlockedBy(criterionName, InventoryChangeTrigger.TriggerInstance.hasItems(items));
	}

	/**
	 * Sets a custom namespace (mod ID) for the recipe.
	 */
	public CookingPotRecipeBuilder setNamespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	public static Identifier getDefaultRecipeId(ItemLike itemLike) {
		return Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(itemLike.asItem()));
	}

	/**
	 * Shorthand for saving recipes in the FD namespace.
	 */
	public void saveToFD(RecipeOutput output) {
		this.setNamespace(FarmersDelight.MODID).save(output);
	}

	public void save(RecipeOutput output) {
		Identifier defaultLocation = getDefaultRecipeId(result);
		Identifier id = Identifier.fromNamespaceAndPath(this.namespace != null ? namespace : defaultLocation.getNamespace(), defaultLocation.getPath()).withPrefix("cooking/");
		save(output, ResourceKey.create(Registries.RECIPE, id));
	}

	@Override
	public ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> defaultId() {
		Identifier id = Identifier.fromNamespaceAndPath(this.namespace != null ? this.namespace : getDefaultRecipeId(this.result).getNamespace(), getDefaultRecipeId(this.result).getPath()).withPrefix("cooking/");
		return ResourceKey.create(Registries.RECIPE, id);
	}

	@Override
	public void save(RecipeOutput output, net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key) {
		Identifier recipeId = key.identifier();
		Advancement.Builder advancementBuilder = output.advancement()
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
				.rewards(AdvancementRewards.Builder.recipe(key))
				.requirements(AdvancementRequirements.Strategy.OR);
		this.criteria.forEach(advancementBuilder::addCriterion);
		CookingPotRecipe recipe = new CookingPotRecipe(
				"",
				this.tab,
				this.ingredients,
				ItemStackTemplate.fromNonEmptyStack(this.resultStack),
				this.container,
				this.experience,
				this.cookingTime
		);
		output.accept(key, recipe, advancementBuilder.build(recipeId.withPrefix("recipes/")));
	}
}
