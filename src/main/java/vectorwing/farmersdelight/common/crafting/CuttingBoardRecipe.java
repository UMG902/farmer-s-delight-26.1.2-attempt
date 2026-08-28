package vectorwing.farmersdelight.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult;
import vectorwing.farmersdelight.common.registry.ModRecipeSerializers;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CuttingBoardRecipe implements Recipe<CuttingBoardRecipeInput>
{
        public static final int MAX_RESULTS = 4;

        private final String group;
        private final Ingredient input;
        private final Ingredient tool;
        private final NonNullList<ChanceResult> results;
        private final Optional<SoundEvent> soundEvent;

        public CuttingBoardRecipe(String group, Ingredient input, Ingredient tool, NonNullList<ChanceResult> results, Optional<SoundEvent> soundEvent) {
                this.group = group;
                this.input = input;
                this.tool = tool;
                this.results = results;
                this.soundEvent = soundEvent;
        }

        @Override
        public boolean matches(CuttingBoardRecipeInput input, Level level) {
                return this.input.test(input.item()) && this.tool.test(input.tool());
        }

        @Override
        public ItemStack assemble(CuttingBoardRecipeInput inv) {
                return this.results.getFirst().stack().copy();
        }

        public boolean isSpecial() {
                return true;
        }

        public String getGroup() {
                return this.group;
        }

        public NonNullList<Ingredient> getIngredients() {
                NonNullList<Ingredient> nonnulllist = NonNullList.create();
                nonnulllist.add(this.input);
                return nonnulllist;
        }

        public Ingredient getTool() {
                return this.tool;
        }

        public ItemStack getResultItem() {
                return this.results.getFirst().stack();
        }

        public List<ItemStack> getResults() {
                return getRollableResults().stream()
                                .map(ChanceResult::stack)
                                .collect(Collectors.toList());
        }

        public NonNullList<ChanceResult> getRollableResults() {
                return this.results;
        }

        public List<ItemStack> rollResults(RandomSource random, int fortuneLevel, RecipeWrapper inventory) {
                List<ItemStack> results = new ArrayList<>();
                NonNullList<ChanceResult> rollableResults = getRollableResults();
                for (ChanceResult output : rollableResults) {
                        ItemStack stack = output.rollOutput(random, fortuneLevel);
                        if (!stack.isEmpty())
                                results.add(stack);
                }
                return results;
        }

        public Optional<SoundEvent> getSoundEvent() {
                return this.soundEvent;
        }

        protected int getMaxInputCount() {
                return 1;
        }

        public boolean canCraftInDimensions(int width, int height) {
                return width * height >= this.getMaxInputCount();
        }

        @Override
        public RecipeSerializer<? extends Recipe<CuttingBoardRecipeInput>> getSerializer() {
                return ModRecipeSerializers.CUTTING.get();
        }

        @Override
        public RecipeType<? extends Recipe<CuttingBoardRecipeInput>> getType() {
                return ModRecipeTypes.CUTTING.get();
        }

        @Override
        public String group() { return this.group; }

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

                CuttingBoardRecipe that = (CuttingBoardRecipe) o;

                if (!getGroup().equals(that.getGroup())) return false;
                if (!input.equals(that.input)) return false;
                if (!getTool().equals(that.getTool())) return false;
                if (!getResults().equals(that.getResults())) return false;
                return Objects.equals(soundEvent, that.soundEvent);
        }

        @Override
        public int hashCode() {
                int result = (getGroup() != null ? getGroup().hashCode() : 0);
                result = 31 * result + input.hashCode();
                result = 31 * result + getTool().hashCode();
                result = 31 * result + getResults().hashCode();
                result = 31 * result + (soundEvent.map(Object::hashCode).orElse(0));
                return result;
        }

        private static DataResult<Ingredient> decodeInputIngredient(List<Ingredient> ingredients) {
                if (ingredients.size() != 1) {
                        return DataResult.error(() -> "Cutting recipes must have exactly one ingredient");
                }
                return DataResult.success(ingredients.getFirst());
        }

        private static DataResult<List<Ingredient>> encodeInputIngredient(Ingredient ingredient) {
                return DataResult.success(List.of(ingredient));
        }

        public static final MapCodec<CuttingBoardRecipe> CODEC = RecordCodecBuilder.mapCodec(
                inst -> inst.group(Codec.STRING.optionalFieldOf("group", "").forGetter(CuttingBoardRecipe::getGroup),
                        Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap(
                                CuttingBoardRecipe::decodeInputIngredient,
                                CuttingBoardRecipe::encodeInputIngredient
                        ).forGetter(cuttingBoardRecipe -> cuttingBoardRecipe.input),
                        Ingredient.CODEC.fieldOf("tool").forGetter(CuttingBoardRecipe::getTool),
                        Codec.list(ChanceResult.CODEC).fieldOf("result").flatXmap(chanceResults -> {
                                if (chanceResults.size() > MAX_RESULTS) {
                                        return DataResult.error(() -> "Too many results for cutting recipe! The maximum quantity of unique results is " + MAX_RESULTS);
                                }
                                NonNullList<ChanceResult> nonNullList = NonNullList.create();
                                nonNullList.addAll(chanceResults);
                                return DataResult.success(nonNullList);
                        }, DataResult::success).forGetter(CuttingBoardRecipe::getRollableResults),
                        SoundEvent.DIRECT_CODEC.optionalFieldOf("sound").forGetter(CuttingBoardRecipe::getSoundEvent))
                .apply(inst, CuttingBoardRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CuttingBoardRecipe> STREAM_CODEC =
                StreamCodec.of(CuttingBoardRecipe::toNetwork, CuttingBoardRecipe::fromNetwork);

        private static CuttingBoardRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
                String group = buffer.readUtf();
                Ingredient inputItem = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                Ingredient tool = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                int i = buffer.readVarInt();
                NonNullList<ChanceResult> results = NonNullList.create();
                for (int j = 0; j < i; j++) results.add(ChanceResult.read(buffer));
                Optional<SoundEvent> soundEvent = Optional.empty();
                if (buffer.readBoolean()) {
                        Holder.Reference<SoundEvent> holder = BuiltInRegistries.SOUND_EVENT.get(buffer.readResourceKey(Registries.SOUND_EVENT)).orElse(null);
                        if (holder != null) soundEvent = Optional.of(holder.value());
                }
                return new CuttingBoardRecipe(group, inputItem, tool, results, soundEvent);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, CuttingBoardRecipe recipe) {
                buffer.writeUtf(recipe.group);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.tool);
                buffer.writeVarInt(recipe.results.size());
                for (ChanceResult result : recipe.results) result.write(buffer);
                if (recipe.soundEvent.isPresent()) {
                        BuiltInRegistries.SOUND_EVENT.getResourceKey(recipe.soundEvent.get()).ifPresentOrElse(rk -> { buffer.writeBoolean(true); buffer.writeResourceKey(rk); }, () -> buffer.writeBoolean(false));
                } else buffer.writeBoolean(false);
        }

}
