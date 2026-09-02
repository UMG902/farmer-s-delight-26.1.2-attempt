package vectorwing.farmersdelight.data;

import com.google.gson.JsonObject;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModItems;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Item model and client item definition datagen for the 26.1 two-layer asset format:
 * <ul>
 *   <li>assets/farmersdelight/models/item/*.json - the actual models (parents, textures)</li>
 *   <li>assets/farmersdelight/items/*.json - client item definitions pointing at a model</li>
 * </ul>
 * Reproduces the checked-in generated resources, including the items whose client item
 * definition points directly at their block model.
 */
public class ItemModels implements DataProvider
{
        public static final String GENERATED = "minecraft:item/generated";
        public static final String HANDHELD = "minecraft:item/handheld";
        public static final Identifier MUG = Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "item/mug");

        /**
         * Items whose client item definition points straight at their block model instead of the
         * (still generated) models/item file. Matches the checked-in generated resources.
         */
        private static final Set<String> DIRECT_BLOCK_ITEM_IDS = Set.of(
                        "canvas_rug", "chocolate_pie", "cooking_pot", "cutting_board", "rope_fence_gate");

        private final PackOutput.PathProvider itemInfoPathProvider;
        private final PackOutput.PathProvider modelPathProvider;

        private final Map<Identifier, ModelInstance> models = new HashMap<>();
        private final Map<Item, ClientItem> itemInfos = new LinkedHashMap<>();

        public ItemModels(PackOutput output) {
                this.itemInfoPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
                this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
                this.registerModels();
                return CompletableFuture.allOf(
                                DataProvider.saveAll(cache, Supplier::get, this.modelPathProvider::json, this.models),
                                DataProvider.saveAll(cache, ClientItem.CODEC,
                                                item -> this.itemInfoPathProvider.json(item.builtInRegistryHolder().key().identifier()), this.itemInfos));
        }

        @Override
        public String getName() {
                return "Farmer's Delight Item Models";
        }

        // Registration ------------------------------

        public void registerModels() {
                Set<Item> items = BuiltInRegistries.ITEM.stream()
                                .filter(i -> FarmersDelight.MODID.equals(BuiltInRegistries.ITEM.getKey(i).getNamespace()))
                                .collect(Collectors.toCollection(LinkedHashSet::new));

                // The skillet's client item definition and models are hand-made (condition + special renderer)
                items.removeIf(item -> itemName(item).equals("skillet"));

                itemGeneratedModel(ModItems.WILD_RICE.get(), blockTexture(itemName(ModItems.WILD_RICE.get()) + "_top"));
                items.remove(ModItems.WILD_RICE.get());

                itemGeneratedModel(ModItems.BROWN_MUSHROOM_COLONY.get(), blockTexture(itemName(ModItems.BROWN_MUSHROOM_COLONY.get()) + "_stage3"));
                items.remove(ModItems.BROWN_MUSHROOM_COLONY.get());

                itemGeneratedModel(ModItems.RED_MUSHROOM_COLONY.get(), blockTexture(itemName(ModItems.RED_MUSHROOM_COLONY.get()) + "_stage3"));
                items.remove(ModItems.RED_MUSHROOM_COLONY.get());

                itemGeneratedModel(ModItems.DEBUG_PUMPKIN_PIE.get(), itemTexture("debug_pumpkin_pie"));
                items.remove(ModItems.DEBUG_PUMPKIN_PIE.get());

                blockBasedModel(ModItems.TATAMI.get(), "_half");
                items.remove(ModItems.TATAMI.get());

                blockBasedModel(ModItems.ORGANIC_COMPOST.get(), "_stage0");
                items.remove(ModItems.ORGANIC_COMPOST.get());

                blockBasedModel(ModItems.ROPE_FENCE.get(), "_inventory");
                items.remove(ModItems.ROPE_FENCE.get());

                // Items that should be held like a mug
                Set<Item> mugItems = Set.of(
                                ModItems.HOT_COCOA.get(),
                                ModItems.APPLE_CIDER.get(),
                                ModItems.MELON_JUICE.get());
                takeAll(items, mugItems::contains).forEach(item -> itemMugModel(item, itemTexture(itemName(item))));

                // Blocks with special item sprites
                Set<Item> spriteBlockItems = Set.of(
                                ModItems.FULL_TATAMI_MAT.get(),
                                ModItems.HALF_TATAMI_MAT.get(),
                                ModItems.ROPE.get(),
                                ModItems.CANVAS_SIGN.get(),
                                ModItems.HANGING_CANVAS_SIGN.get(),
                                ModItems.WHITE_CANVAS_SIGN.get(),
                                ModItems.WHITE_HANGING_CANVAS_SIGN.get(),
                                ModItems.ORANGE_CANVAS_SIGN.get(),
                                ModItems.ORANGE_HANGING_CANVAS_SIGN.get(),
                                ModItems.MAGENTA_CANVAS_SIGN.get(),
                                ModItems.MAGENTA_HANGING_CANVAS_SIGN.get(),
                                ModItems.LIGHT_BLUE_CANVAS_SIGN.get(),
                                ModItems.LIGHT_BLUE_HANGING_CANVAS_SIGN.get(),
                                ModItems.YELLOW_CANVAS_SIGN.get(),
                                ModItems.YELLOW_HANGING_CANVAS_SIGN.get(),
                                ModItems.LIME_CANVAS_SIGN.get(),
                                ModItems.LIME_HANGING_CANVAS_SIGN.get(),
                                ModItems.PINK_CANVAS_SIGN.get(),
                                ModItems.PINK_HANGING_CANVAS_SIGN.get(),
                                ModItems.GRAY_CANVAS_SIGN.get(),
                                ModItems.GRAY_HANGING_CANVAS_SIGN.get(),
                                ModItems.LIGHT_GRAY_CANVAS_SIGN.get(),
                                ModItems.LIGHT_GRAY_HANGING_CANVAS_SIGN.get(),
                                ModItems.CYAN_CANVAS_SIGN.get(),
                                ModItems.CYAN_HANGING_CANVAS_SIGN.get(),
                                ModItems.PURPLE_CANVAS_SIGN.get(),
                                ModItems.PURPLE_HANGING_CANVAS_SIGN.get(),
                                ModItems.BLUE_CANVAS_SIGN.get(),
                                ModItems.BLUE_HANGING_CANVAS_SIGN.get(),
                                ModItems.BROWN_CANVAS_SIGN.get(),
                                ModItems.BROWN_HANGING_CANVAS_SIGN.get(),
                                ModItems.GREEN_CANVAS_SIGN.get(),
                                ModItems.GREEN_HANGING_CANVAS_SIGN.get(),
                                ModItems.RED_CANVAS_SIGN.get(),
                                ModItems.RED_HANGING_CANVAS_SIGN.get(),
                                ModItems.BLACK_CANVAS_SIGN.get(),
                                ModItems.BLACK_HANGING_CANVAS_SIGN.get(),
                                ModItems.APPLE_PIE.get(),
                                ModItems.SWEET_BERRY_CHEESECAKE.get(),
                                ModItems.CHOCOLATE_PIE.get(),
                                ModItems.CABBAGE_SEEDS.get(),
                                ModItems.TOMATO_SEEDS.get(),
                                ModItems.ONION.get(),
                                ModItems.RICE.get(),
                                ModItems.ROAST_CHICKEN_BLOCK.get(),
                                ModItems.STUFFED_PUMPKIN_BLOCK.get(),
                                ModItems.HONEY_GLAZED_HAM_BLOCK.get(),
                                ModItems.SHEPHERDS_PIE_BLOCK.get(),
                                ModItems.GLEAMING_SALAD_BLOCK.get(),
                                ModItems.RICE_ROLL_MEDLEY_BLOCK.get()
                );
                takeAll(items, spriteBlockItems::contains).forEach(item -> itemGeneratedModel(item, itemTexture(itemName(item))));

                // Blocks with flat block textures for their items
                Set<Item> flatBlockItems = Set.of(
                                ModItems.SAFETY_NET.get(),
                                ModItems.SANDY_SHRUB.get(),
                                ModItems.WILD_BEETROOTS.get(),
                                ModItems.WILD_CABBAGES.get(),
                                ModItems.WILD_CARROTS.get(),
                                ModItems.WILD_ONIONS.get(),
                                ModItems.WILD_POTATOES.get(),
                                ModItems.WILD_TOMATOES.get()
                );
                takeAll(items, flatBlockItems::contains).forEach(item -> itemGeneratedModel(item, blockTexture(itemName(item))));

                // Blocks whose items look alike
                takeAll(items, item -> item instanceof BlockItem).forEach(item -> blockBasedModel(item, ""));

                // Handheld items
                Set<Item> handheldItems = Set.of(
                                ModItems.BARBECUE_STICK.get(),
                                ModItems.HAM.get(),
                                ModItems.SMOKED_HAM.get(),
                                ModItems.FLINT_KNIFE.get(),
                                ModItems.COPPER_KNIFE.get(),
                                ModItems.IRON_KNIFE.get(),
                                ModItems.DIAMOND_KNIFE.get(),
                                ModItems.GOLDEN_KNIFE.get(),
                                ModItems.NETHERITE_KNIFE.get()
                );
                takeAll(items, handheldItems::contains).forEach(item -> itemHandheldModel(item, itemTexture(itemName(item))));

                // Generated items
                items.forEach(item -> itemGeneratedModel(item, itemTexture(itemName(item))));
        }

        // Model functions ------------------------------

        /**
         * Emits models/item/<name>.json with a parent and optional layer0 texture, and registers the
         * client item definition pointing at it (or directly at the block model for override items).
         */
        public void emitItemModel(Item item, Identifier parent, @Nullable Identifier layer0) {
                String name = itemName(item);
                Identifier modelId = itemModel(name);
                JsonObject json = new JsonObject();
                json.addProperty("parent", parent.toString());
                if (layer0 != null) {
                        JsonObject textureObject = new JsonObject();
                        textureObject.addProperty("layer0", layer0.toString());
                        json.add("textures", textureObject);
                }
                this.models.put(modelId, () -> json);

                Identifier definitionTarget = DIRECT_BLOCK_ITEM_IDS.contains(name) ? blockModel(name) : modelId;
                ItemModel.Unbaked unbaked = ItemModelUtils.plainModel(definitionTarget);
                ClientItem previous = this.itemInfos.put(item, new ClientItem(unbaked, ClientItem.Properties.DEFAULT));
                if (previous != null) {
                        throw new IllegalStateException("Duplicate item model definition for " + item);
                }
        }

        public void blockBasedModel(Item item, String suffix) {
                emitItemModel(item, blockModel(itemName(item) + suffix), null);
        }

        public void itemHandheldModel(Item item, Identifier texture) {
                emitItemModel(item, Identifier.withDefaultNamespace("item/handheld"), texture);
        }

        public void itemGeneratedModel(Item item, Identifier texture) {
                emitItemModel(item, Identifier.withDefaultNamespace("item/generated"), texture);
        }

        public void itemMugModel(Item item, Identifier texture) {
                emitItemModel(item, MUG, texture);
        }

        private String itemName(Item item) {
                return BuiltInRegistries.ITEM.getKey(item).getPath();
        }

        public Identifier blockTexture(String path) {
                return Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + path);
        }

        public Identifier itemTexture(String path) {
                return Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "item/" + path);
        }

        public Identifier itemModel(String path) {
                return Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "item/" + path);
        }

        public Identifier blockModel(String path) {
                return Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + path);
        }

        public static <T> List<T> takeAll(Set<? extends T> src, Predicate<T> pred) {
                List<T> ret = new ArrayList<>();
                Iterator<T> iter = (Iterator<T>) src.iterator();
                while (iter.hasNext()) {
                        T item = iter.next();
                        if (pred.test(item)) {
                                iter.remove();
                                ret.add(item);
                        }
                }
                return ret;
        }
}
