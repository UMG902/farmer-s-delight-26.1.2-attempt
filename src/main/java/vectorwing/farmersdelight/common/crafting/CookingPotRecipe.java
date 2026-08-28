package vectorwing.farmersdelight.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import java.util.List;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeSerializers;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import javax.annotation.Nullable;
import java.util.Optional;

public class CookingPotRecipe implements Recipe<RecipeInput>
{
        public static final int INPUT_SLOTS = 6;

        private final String group;
        private final CookingPotRecipeBookTab tab;
        private final NonNullList<Ingredient> inputItems;
        private final ItemStackTemplate output;
        @Nullable
        private final ItemStackTemplate container;
        @Nullable
        private final ItemStackTemplate containerOverride;
        private final float experience;
        private final int cookTime;

        public CookingPotRecipe(String group, @Nullable CookingPotRecipeBookTab tab, NonNullList<Ingredient> inputItems, ItemStackTemplate output, ItemStackTemplate container, float experience, int cookTime) {
                this.group = group;
                this.tab = tab;
                this.inputItems = inputItems;
                this.output = output;
                this.containerOverride = container;

                if (container != null) {
                        this.container = container;
                } else if (output.getCraftingRemainder() != null) {
                        this.container = output.getCraftingRemainder();
                } else {
                        this.container = null;
                }
                this.experience = experience;
                this.cookTime = cookTime;
        }

        public String getGroup() {
                return this.group;
        }

        @Nullable
        public CookingPotRecipeBookTab getRecipeBookTab() {
                return this.tab;
        }

        public NonNullList<Ingredient> getIngredients() {
                return this.inputItems;
        }

        public ItemStack getResultItem() {
                return this.output.create();
        }

        public ItemStack getOutputContainer() {
                return this.container == null ? ItemStack.EMPTY : this.container.create();
        }

        public ItemStack getContainerOverride() {
                return this.containerOverride == null ? ItemStack.EMPTY : this.containerOverride.create();
        }

        @Override
        public ItemStack assemble(RecipeInput inv) {
                return this.output.create();
        }

        public float getExperience() {
                return this.experience;
        }

        public int getCookTime() {
                return this.cookTime;
        }

        @Override
        public boolean matches(RecipeInput inv, Level level) {
                java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
                int i = 0;

                for (int j = 0; j < INPUT_SLOTS; ++j) {
                        ItemStack itemstack = inv.getItem(j);
                        if (!itemstack.isEmpty()) {
                                ++i;
                                inputs.add(itemstack);
                        }
                }
                return i == this.inputItems.size() && RecipeMatcher.findMatches(inputs, this.inputItems) != null;
        }

        public boolean canCraftInDimensions(int width, int height) {
                return width * height >= this.inputItems.size();
        }

        @Override
        public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
                return ModRecipeSerializers.COOKING.get();
        }

        @Override
        public RecipeType<? extends Recipe<RecipeInput>> getType() {
                return ModRecipeTypes.COOKING.get();
        }

        public ItemStack getToastSymbol() {
                return new ItemStack(ModItems.COOKING_POT.get());
        }

        @Override
        public String group() { return this.group; }

        @Override
        public boolean isSpecial() { return true; }

        @Override
        public boolean showNotification() { return false; }

        @Override
        public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }

        @Override
        public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }

        @Override
        public List<RecipeDisplay> display() { return List.of(); }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                CookingPotRecipe that = (CookingPotRecipe) o;

                if (Float.compare(that.getExperience(), getExperience()) != 0) return false;
                if (getCookTime() != that.getCookTime()) return false;
                if (!getGroup().equals(that.getGroup())) return false;
                if (tab != that.tab) return false;
                if (!inputItems.equals(that.inputItems)) return false;
                if (!output.equals(that.output)) return false;
                return container.equals(that.container);
        }

        @Override
        public int hashCode() {
                int result = getGroup().hashCode();
                result = 31 * result + (getRecipeBookTab() != null ? getRecipeBookTab().hashCode() : 0);
                result = 31 * result + inputItems.hashCode();
                result = 31 * result + output.hashCode();
                result = 31 * result + container.hashCode();
                result = 31 * result + (getExperience() != 0.0f ? Float.floatToIntBits(getExperience()) : 0);
                result = 31 * result + getCookTime();
                return result;
        }

        public static final MapCodec<CookingPotRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(CookingPotRecipe::getGroup),
                        CookingPotRecipeBookTab.CODEC.optionalFieldOf("recipe_book_tab", CookingPotRecipeBookTab.MISC).forGetter(CookingPotRecipe::getRecipeBookTab),
                        Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(CookingPotRecipe::getIngredients),
                        ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.output),
                        ItemStackTemplate.CODEC.optionalFieldOf("container").forGetter(r -> Optional.ofNullable(r.containerOverride)),
                        Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(CookingPotRecipe::getExperience),
                        Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(CookingPotRecipe::getCookTime)
                ).apply(inst, (group, tab, ingredients, output, container, experience, cookTime) -> {
                        NonNullList<Ingredient> inputs = NonNullList.create();
                        inputs.addAll(ingredients);
                        return new CookingPotRecipe(group, tab, inputs, output, container.orElse(null), experience, cookTime);
                }));

        public static final StreamCodec<RegistryFriendlyByteBuf, CookingPotRecipe> STREAM_CODEC = StreamCodec.of(
                CookingPotRecipe::toNetwork, CookingPotRecipe::fromNetwork);

        private static CookingPotRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
                String group = buffer.readUtf();
                CookingPotRecipeBookTab tab = CookingPotRecipeBookTab.findByName(buffer.readUtf());
                int i = buffer.readVarInt();
                NonNullList<Ingredient> inputItems = NonNullList.create();
                for (int j = 0; j < i; j++) inputItems.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
                ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(buffer);
                ItemStackTemplate container = buffer.readBoolean() ? ItemStackTemplate.STREAM_CODEC.decode(buffer) : null;
                float experience = buffer.readFloat();
                int cookTime = buffer.readVarInt();
                return new CookingPotRecipe(group, tab, inputItems, output, container, experience, cookTime);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, CookingPotRecipe recipe) {
                buffer.writeUtf(recipe.group);
                buffer.writeUtf(recipe.tab != null ? recipe.tab.toString() : "");
                buffer.writeVarInt(recipe.inputItems.size());
                for (Ingredient ingredient : recipe.inputItems) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
                }
                ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.output);
                boolean hasContainer = recipe.containerOverride != null;
                buffer.writeBoolean(hasContainer);
                if (hasContainer) ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.containerOverride);
                buffer.writeFloat(recipe.experience);
                buffer.writeVarInt(recipe.cookTime);
        }

}
