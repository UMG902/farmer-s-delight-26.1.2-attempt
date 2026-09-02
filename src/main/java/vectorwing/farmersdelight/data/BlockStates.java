package vectorwing.farmersdelight.data;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function3;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.BuddingBushBlock;
import vectorwing.farmersdelight.common.block.CabinetBlock;
import vectorwing.farmersdelight.common.block.CookingPotBlock;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;
import vectorwing.farmersdelight.common.block.OnionBlock;
import vectorwing.farmersdelight.common.block.OrganicCompostBlock;
import vectorwing.farmersdelight.common.block.PieBlock;
import vectorwing.farmersdelight.common.block.RiceBlock;
import vectorwing.farmersdelight.common.block.RicePaniclesBlock;
import vectorwing.farmersdelight.common.block.RichSoilFarmlandBlock;
import vectorwing.farmersdelight.common.block.RopeBlock;
import vectorwing.farmersdelight.common.block.SkilletBlock;
import vectorwing.farmersdelight.common.block.StoveBlock;
import vectorwing.farmersdelight.common.block.TatamiBlock;
import vectorwing.farmersdelight.common.block.TatamiMatBlock;
import vectorwing.farmersdelight.common.block.TomatoBlock;
import vectorwing.farmersdelight.common.block.state.CookingPotSupport;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Block state and block model datagen, rebuilt against the 26.1 vanilla datagen API
 * (net.minecraft.client.data.models), which replaced the old BlockStateProvider stack.
 *
 * Rotation conventions intentionally follow the checked-in generated resources:
 * - horizontal blocks use plain Y rotations (north = 0, east = 90...)
 * - FACING blocks (baskets, rice bale) use column-style rotations (north = x 90)
 * - bed-style blocks (full tatami mat, rope fence gate) offset Y rotations by 180 degrees
 */
public class BlockStates implements DataProvider
{
        public static final VariantMutator NOP = v -> v;
        public static final VariantMutator X_ROT_90 = VariantMutator.X_ROT.withValue(Quadrant.R90);
        public static final VariantMutator X_ROT_180 = VariantMutator.X_ROT.withValue(Quadrant.R180);
        public static final VariantMutator X_ROT_270 = VariantMutator.X_ROT.withValue(Quadrant.R270);
        public static final VariantMutator Y_ROT_90 = VariantMutator.Y_ROT.withValue(Quadrant.R90);
        public static final VariantMutator Y_ROT_180 = VariantMutator.Y_ROT.withValue(Quadrant.R180);
        public static final VariantMutator Y_ROT_270 = VariantMutator.Y_ROT.withValue(Quadrant.R270);

        public static final PropertyDispatch<VariantMutator> ROTATION_HORIZONTAL_FACING = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                        .select(Direction.EAST, Y_ROT_90)
                        .select(Direction.SOUTH, Y_ROT_180)
                        .select(Direction.WEST, Y_ROT_270)
                        .select(Direction.NORTH, NOP);

        public static final PropertyDispatch<VariantMutator> ROTATION_HORIZONTAL_FACING_ALT = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                        .select(Direction.SOUTH, NOP)
                        .select(Direction.WEST, Y_ROT_90)
                        .select(Direction.NORTH, Y_ROT_180)
                        .select(Direction.EAST, Y_ROT_270);

        public static final PropertyDispatch<VariantMutator> ROTATIONS_COLUMN_WITH_FACING = PropertyDispatch.modify(BlockStateProperties.FACING)
                        .select(Direction.DOWN, X_ROT_180)
                        .select(Direction.UP, NOP)
                        .select(Direction.NORTH, X_ROT_90)
                        .select(Direction.SOUTH, X_ROT_90.then(Y_ROT_180))
                        .select(Direction.WEST, X_ROT_90.then(Y_ROT_270))
                        .select(Direction.EAST, X_ROT_90.then(Y_ROT_90));

        public static final PropertyDispatch<VariantMutator> ROTATION_PILLAR = PropertyDispatch.modify(BlockStateProperties.AXIS)
                        .select(Direction.Axis.X, X_ROT_90.then(Y_ROT_90))
                        .select(Direction.Axis.Y, NOP)
                        .select(Direction.Axis.Z, X_ROT_90);

        private final PackOutput.PathProvider blockStatePathProvider;
        private final PackOutput.PathProvider modelPathProvider;

        private final Map<Block, BlockStateModelDispatcher> blockStateDefinitions = new HashMap<>();
        private final Map<Identifier, ModelInstance> models = new HashMap<>();

        public BlockStates(PackOutput output) {
                this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
                this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
                this.registerStatesAndModels();
                return CompletableFuture.allOf(
                                DataProvider.saveAll(cache, BlockStateModelDispatcher.CODEC,
                                                block -> this.blockStatePathProvider.json(block.builtInRegistryHolder().key().identifier()), this.blockStateDefinitions),
                                DataProvider.saveAll(cache, Supplier::get, this.modelPathProvider::json, this.models));
        }

        @Override
        public String getName() {
                return "Farmer's Delight Block States and Models";
        }

        // Registration ------------------------------

        public void registerStatesAndModels() {
                simpleBlock(ModBlocks.SAFETY_NET.get(), existingModel(blockName(ModBlocks.SAFETY_NET.get())));
                simpleBlock(ModBlocks.CANVAS_RUG.get(), existingModel(blockName(ModBlocks.CANVAS_RUG.get())));

                // Rice bag: fully textured cube emitted by the generator
                emitModel(blockModel(ModBlocks.RICE_BAG.get()), mcBlock("cube"), null, Map.of(
                                "particle", blockTexture(blockName(ModBlocks.RICE_BAG.get()) + "_top"),
                                "down", blockTexture(blockName(ModBlocks.RICE_BAG.get()) + "_bottom"),
                                "up", blockTexture(blockName(ModBlocks.RICE_BAG.get()) + "_top"),
                                "north", blockTexture(blockName(ModBlocks.RICE_BAG.get()) + "_side_tied"),
                                "south", blockTexture(blockName(ModBlocks.RICE_BAG.get()) + "_side_tied"),
                                "east", blockTexture(blockName(ModBlocks.RICE_BAG.get()) + "_side"),
                                "west", blockTexture(blockName(ModBlocks.RICE_BAG.get()) + "_side")));
                simpleBlock(ModBlocks.RICE_BAG.get(), blockModel(ModBlocks.RICE_BAG.get()));

                customDirectionalBlock(ModBlocks.WOODEN_BASKET.get(), state -> modelBasket(blockName(ModBlocks.WOODEN_BASKET.get())));
                customDirectionalBlock(ModBlocks.BAMBOO_BASKET.get(), state -> modelBasket(blockName(ModBlocks.BAMBOO_BASKET.get())));
                customDirectionalBlock(ModBlocks.RICE_BALE.get(), state -> modelCubeBottomTop(blockName(ModBlocks.RICE_BALE.get())));
                customHorizontalBlock(ModBlocks.CUTTING_BOARD.get(), state -> existingModel(blockName(ModBlocks.CUTTING_BOARD.get())));

                customHorizontalBlock(ModBlocks.HALF_TATAMI_MAT.get(), state -> existingModel("tatami_mat_half"));

                stageBlock(ModBlocks.BROWN_MUSHROOM_COLONY.get(), MushroomColonyBlock.COLONY_AGE);
                stageBlock(ModBlocks.RED_MUSHROOM_COLONY.get(), MushroomColonyBlock.COLONY_AGE);

                customStageBlock(ModBlocks.CABBAGE_CROP.get(), blockTexture("template_crop_cross"), "cross", CropBlock.AGE, new ArrayList<>());
                customStageBlock(ModBlocks.ONION_CROP.get(), mcBlock("crop"), "crop", CropBlock.AGE, Arrays.asList(0, 0, 1, 1, 2, 2, 2, 3));
                customStageBlock(ModBlocks.BUDDING_TOMATO_CROP.get(), blockTexture("template_crop_cross"), "cross", BuddingBushBlock.AGE, Arrays.asList(0, 1, 2, 3, 3));
                tomatoBlock(ModBlocks.TOMATO_CROP.get(), TomatoBlock.VINE_AGE, TomatoBlock.ROPELOGGED);
                ropedTomatoBlock(ModBlocks.TOMATO_CROP_ON_ROPE.get(), TomatoBlock.VINE_AGE);
                riceRootBlock(ModBlocks.RICE_CROP.get());
                stageBlock(ModBlocks.RICE_CROP_PANICLES.get(), RicePaniclesBlock.RICE_AGE);

                crateBlock(ModBlocks.CARROT_CRATE.get(), "carrot");
                crateBlock(ModBlocks.POTATO_CRATE.get(), "potato");
                crateBlock(ModBlocks.BEETROOT_CRATE.get(), "beetroot");
                crateBlock(ModBlocks.CABBAGE_CRATE.get(), "cabbage");
                crateBlock(ModBlocks.TOMATO_CRATE.get(), "tomato");
                crateBlock(ModBlocks.ONION_CRATE.get(), "onion");

                emitModel(blockModel(ModBlocks.STRAW_BALE.get()), mcBlock("cube_column"), null, Map.of(
                                "end", blockTexture("straw_bale_end"),
                                "side", blockTexture("straw_bale_side")));
                emitModel(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/straw_bale_horizontal"),
                                mcBlock("cube_column_horizontal"), null, Map.of(
                                "end", blockTexture("straw_bale_end"),
                                "side", blockTexture("straw_bale_side")));
                axisBlock((RotatedPillarBlock) ModBlocks.STRAW_BALE.get());

                organicCompostBlock(ModBlocks.ORGANIC_COMPOST.get());
                simpleBlockAllYRotations(ModBlocks.RICH_SOIL.get());
                farmlandBlock(ModBlocks.RICH_SOIL_FARMLAND.get(), ModBlocks.RICH_SOIL.get());

                this.accept(this.multipart(ModBlocks.ROPE.get())
                                .with(variant(existingModel("rope_post")))
                                .with(term(RopeBlock.TIED_TO_BELL, true), variant(existingModel("rope_bell_tie")))
                                .with(term(RopeBlock.NORTH, true), variant(existingModel("rope_side")))
                                .with(term(RopeBlock.EAST, true), variant(existingModel("rope_side")).with(Y_ROT_90))
                                .with(term(RopeBlock.SOUTH, true), variant(existingModel("rope_side_alt")))
                                .with(term(RopeBlock.WEST, true), variant(existingModel("rope_side_alt")).with(Y_ROT_90)));

                this.accept(this.multipart(ModBlocks.ROPE_FENCE.get())
                                .with(variant(existingModel("rope_fence_post")))
                                .with(term(FenceBlock.NORTH, true), variant(existingModel("rope_fence_side")))
                                .with(term(FenceBlock.EAST, true), variant(existingModel("rope_fence_side")).with(Y_ROT_90))
                                .with(term(FenceBlock.SOUTH, true), variant(existingModel("rope_fence_side_alt")))
                                .with(term(FenceBlock.WEST, true), variant(existingModel("rope_fence_side_alt")).with(Y_ROT_90)));

                ropeFenceGateBlock(ModBlocks.ROPE_FENCE_GATE.get());

                // Full tatami mat: bed-style head/foot models, Y rotations offset by 180 degrees
                this.accept(this.dispatch(ModBlocks.FULL_TATAMI_MAT.get(), variant(existingModel("tatami_mat_foot")))
                                .with(PropertyDispatch.modify(TatamiMatBlock.PART, BlockStateProperties.HORIZONTAL_FACING).generate((part, facing) ->
                                                modelSwitch(existingModel(part == BedPart.HEAD ? "tatami_mat_head" : "tatami_mat_foot")).then(yRotFromHorizontal(facing)))));

                // Tatami: column-style rotations with plain Y; paired mats alternate even/odd models
                emitModel(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/tatami_half"),
                mcBlock("cube_all"), null, Map.of("all", blockTexture("tatami_mat_half")));
this.accept(this.dispatch(ModBlocks.TATAMI.get(), variant(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/tatami_half")))
                                .with(PropertyDispatch.modify(TatamiBlock.FACING, TatamiBlock.PAIRED).generate((BiFunction<Direction, Boolean, VariantMutator>) (dir, paired) -> {
                                        String model = !paired ? "tatami_half" : (dir.get3DDataValue() % 2 == 0 ? "tatami_even" : "tatami_odd");
                                        return switch (dir) {
                                                case DOWN -> X_ROT_180.then(modelSwitch(existingModel(model)));
                                                case UP -> modelSwitch(existingModel(model));
                                                default -> X_ROT_90.then(yRotFromHorizontalPlus180(dir)).then(modelSwitch(existingModel(model)));
                                        };
                                })));

                cabinetBlock(ModBlocks.OAK_CABINET.get(), "oak");
                cabinetBlock(ModBlocks.BIRCH_CABINET.get(), "birch");
                cabinetBlock(ModBlocks.SPRUCE_CABINET.get(), "spruce");
                cabinetBlock(ModBlocks.JUNGLE_CABINET.get(), "jungle");
                cabinetBlock(ModBlocks.ACACIA_CABINET.get(), "acacia");
                cabinetBlock(ModBlocks.DARK_OAK_CABINET.get(), "dark_oak");
                cabinetBlock(ModBlocks.MANGROVE_CABINET.get(), "mangrove");
                cabinetBlock(ModBlocks.CHERRY_CABINET.get(), "cherry");
                cabinetBlock(ModBlocks.BAMBOO_CABINET.get(), "bamboo");
                cabinetBlock(ModBlocks.CRIMSON_CABINET.get(), "crimson");
                cabinetBlock(ModBlocks.WARPED_CABINET.get(), "warped");
                cabinetBlock(ModBlocks.PALE_OAK_CABINET.get(), "pale_oak");

                pieBlock(ModBlocks.APPLE_PIE.get());
                customPieBlock(ModBlocks.CHOCOLATE_PIE.get());
                pieBlock(ModBlocks.SWEET_BERRY_CHEESECAKE.get());
                pieBlock(ModBlocks.PUMPKIN_PIE.get());

                feastBlock((FeastBlock) ModBlocks.STUFFED_PUMPKIN_BLOCK.get());
                feastBlock((FeastBlock) ModBlocks.ROAST_CHICKEN_BLOCK.get());
                feastBlock((FeastBlock) ModBlocks.HONEY_GLAZED_HAM_BLOCK.get());
                feastBlock((FeastBlock) ModBlocks.SHEPHERDS_PIE_BLOCK.get());
                feastBlock((FeastBlock) ModBlocks.GLEAMING_SALAD_BLOCK.get());
                feastBlock((FeastBlock) ModBlocks.RICE_ROLL_MEDLEY_BLOCK.get());

                wildCropBlock(ModBlocks.SANDY_SHRUB.get(), true);
                wildCropBlock(ModBlocks.WILD_BEETROOTS.get(), false);
                wildCropBlock(ModBlocks.WILD_CABBAGES.get(), false);
                wildCropBlock(ModBlocks.WILD_POTATOES.get(), false);
                wildCropBlock(ModBlocks.WILD_TOMATOES.get(), false);
                wildCropBlock(ModBlocks.WILD_CARROTS.get(), false);
                wildCropBlock(ModBlocks.WILD_ONIONS.get(), false);
                doublePlantBlock(ModBlocks.WILD_RICE.get());

                cookingPotBlock(ModBlocks.COOKING_POT.get());
                skilletBlock(ModBlocks.SKILLET.get());
                stoveBlock(ModBlocks.STOVE.get());

                // Canvas signs: every sign block reuses the hand-made canvas sign model
                for (Block sign : CANVAS_SIGNS) {
                        simpleBlock(sign, existingModel(blockName(ModBlocks.CANVAS_SIGN.get())));
                }
        }

        private static final List<Block> CANVAS_SIGNS = List.of(
                        ModBlocks.CANVAS_SIGN.get(), ModBlocks.HANGING_CANVAS_SIGN.get(),
                        ModBlocks.CANVAS_WALL_SIGN.get(), ModBlocks.HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.WHITE_CANVAS_SIGN.get(), ModBlocks.WHITE_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.WHITE_CANVAS_WALL_SIGN.get(), ModBlocks.WHITE_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.ORANGE_CANVAS_SIGN.get(), ModBlocks.ORANGE_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.ORANGE_CANVAS_WALL_SIGN.get(), ModBlocks.ORANGE_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.MAGENTA_CANVAS_SIGN.get(), ModBlocks.MAGENTA_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.MAGENTA_CANVAS_WALL_SIGN.get(), ModBlocks.MAGENTA_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.LIGHT_BLUE_CANVAS_SIGN.get(), ModBlocks.LIGHT_BLUE_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.LIGHT_BLUE_CANVAS_WALL_SIGN.get(), ModBlocks.LIGHT_BLUE_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.YELLOW_CANVAS_SIGN.get(), ModBlocks.YELLOW_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.YELLOW_CANVAS_WALL_SIGN.get(), ModBlocks.YELLOW_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.LIME_CANVAS_SIGN.get(), ModBlocks.LIME_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.LIME_CANVAS_WALL_SIGN.get(), ModBlocks.LIME_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.PINK_CANVAS_SIGN.get(), ModBlocks.PINK_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.PINK_CANVAS_WALL_SIGN.get(), ModBlocks.PINK_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.GRAY_CANVAS_SIGN.get(), ModBlocks.GRAY_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.GRAY_CANVAS_WALL_SIGN.get(), ModBlocks.GRAY_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.LIGHT_GRAY_CANVAS_SIGN.get(), ModBlocks.LIGHT_GRAY_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.LIGHT_GRAY_CANVAS_WALL_SIGN.get(), ModBlocks.LIGHT_GRAY_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.CYAN_CANVAS_SIGN.get(), ModBlocks.CYAN_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.CYAN_CANVAS_WALL_SIGN.get(), ModBlocks.CYAN_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.PURPLE_CANVAS_SIGN.get(), ModBlocks.PURPLE_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.PURPLE_CANVAS_WALL_SIGN.get(), ModBlocks.PURPLE_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.BLUE_CANVAS_SIGN.get(), ModBlocks.BLUE_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.BLUE_CANVAS_WALL_SIGN.get(), ModBlocks.BLUE_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.BROWN_CANVAS_SIGN.get(), ModBlocks.BROWN_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.BROWN_CANVAS_WALL_SIGN.get(), ModBlocks.BROWN_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.GREEN_CANVAS_SIGN.get(), ModBlocks.GREEN_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.GREEN_CANVAS_WALL_SIGN.get(), ModBlocks.GREEN_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.RED_CANVAS_SIGN.get(), ModBlocks.RED_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.RED_CANVAS_WALL_SIGN.get(), ModBlocks.RED_HANGING_CANVAS_WALL_SIGN.get(),
                        ModBlocks.BLACK_CANVAS_SIGN.get(), ModBlocks.BLACK_HANGING_CANVAS_SIGN.get(),
                        ModBlocks.BLACK_CANVAS_WALL_SIGN.get(), ModBlocks.BLACK_HANGING_CANVAS_WALL_SIGN.get());

        // Collectors and emission ------------------------------

        private static Identifier mcBlock(String path) {
                return Identifier.withDefaultNamespace("block/" + path);
        }

        private static Identifier blockTexture(String path) {
                return Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + path);
        }

        private String blockName(Block block) {
                return BuiltInRegistries.BLOCK.getKey(block).getPath();
        }

        /** References a model that is hand-made and checked into src/main/resources; nothing is emitted. */
        private static Identifier existingModel(String path) {
                return Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + path);
        }

        private static Identifier blockModel(Block block) {
                return Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + BuiltInRegistries.BLOCK.getKey(block).getPath());
        }

        /** Emits a model JSON with the given parent, textures and optional render type. */
        private void emitModel(Identifier id, @Nullable Identifier parent, @Nullable String renderType, @Nullable Map<String, Identifier> textures) {
                this.models.put(id, () -> {
                        JsonObject json = new JsonObject();
                        if (parent != null) {
                                json.addProperty("parent", parent.toString());
                        }
                        if (renderType != null) {
                                json.addProperty("render_type", renderType);
                        }
                        if (textures != null && !textures.isEmpty()) {
                                JsonObject textureObject = new JsonObject();
                                textures.forEach((key, value) -> textureObject.addProperty(key, value.toString()));
                                json.add("textures", textureObject);
                        }
                        return json;
                });
        }

        private static MultiVariant variant(Identifier model) {
                return new MultiVariant(WeightedList.of(new Variant(model)));
        }

        private static MultiVariant variants(Variant... modelVariants) {
                return new MultiVariant(WeightedList.of(Arrays.stream(modelVariants).map(v -> new Weighted<>(v, 1)).toList()));
        }

        private static VariantMutator modelSwitch(Identifier model) {
                return VariantMutator.MODEL.withValue(model);
        }

        private static VariantMutator yRot(float yDegrees) {
                return VariantMutator.Y_ROT.withValue(Quadrant.parseJson((int) yDegrees));
        }

        private static VariantMutator yRotFromHorizontal(Direction facing) {
                return yRot(facing.toYRot());
        }

        /** Upstream horizontal rotation convention: toYRot + 180 (north = 0, east = 90...). */
        private static VariantMutator yRotFromHorizontalPlus180(Direction facing) {
                return yRot((facing.toYRot() + 180F) % 360F);
        }

        private static ConditionBuilder term(Property<Boolean> property, boolean value) {
                return new ConditionBuilder().term(property, value);
        }

        private void accept(BlockModelDefinitionGenerator generator) {
                Block block = generator.block();
                BlockStateModelDispatcher previous = this.blockStateDefinitions.put(block, generator.create());
                if (previous != null) {
                        throw new IllegalStateException("Duplicate blockstate definition for " + block);
                }
        }

        private MultiVariantGenerator.Empty dispatch(Block block) {
                return MultiVariantGenerator.dispatch(block);
        }

        private MultiVariantGenerator dispatch(Block block, MultiVariant initial) {
                return MultiVariantGenerator.dispatch(block, initial);
        }

        private MultiPartGenerator multipart(Block block) {
                return MultiPartGenerator.multiPart(block);
        }

        // Block patterns ------------------------------

        /** A block whose every state renders one fixed model. */
        public void simpleBlock(Block block, Identifier model) {
                this.accept(this.dispatch(block, variant(model)));
        }

        /** A full cube emitted by the generator, rendered with all four Y rotations. */
        public void simpleBlockAllYRotations(Block block) {
                String name = blockName(block);
                this.emitModel(blockModel(block), mcBlock("cube_all"), null, Map.of("all", blockTexture(name)));
                MultiVariant rotated = variants(
                                new Variant(blockModel(block)),
                                new Variant(blockModel(block)).withYRot(Quadrant.R90),
                                new Variant(blockModel(block)).withYRot(Quadrant.R180),
                                new Variant(blockModel(block)).withYRot(Quadrant.R270));
                this.accept(this.dispatch(block, rotated));
        }

        /** Directional blocks (down = x 180, up = none, horizontal = x 90 + plain Y). */
        public void customDirectionalBlock(Block block, Function<BlockState, Identifier> modelFunc) {
                this.accept(this.dispatch(block, variant(modelFunc.apply(block.defaultBlockState()))).with(ROTATIONS_COLUMN_WITH_FACING));
        }

        /** Horizontal blocks with a fixed model, rotated with plain Y rotations. */
        public void customHorizontalBlock(Block block, Function<BlockState, Identifier> modelFunc) {
                this.accept(this.dispatch(block, variant(modelFunc.apply(block.defaultBlockState()))).with(ROTATION_HORIZONTAL_FACING));
        }

        /** Age-based crop stages rendered as cross models with cutout rendering. */
        public void stageBlock(Block block, IntegerProperty ageProperty) {
                this.accept(this.dispatch(block, variant(existingModel(blockName(block) + "_stage0"))).with(PropertyDispatch.modify(ageProperty).generate(age -> {
                        String stageName = blockName(block) + "_stage" + age;
                        return modelSwitch(this.crossModel(stageName, stageName));
                })));
        }

        /** Age-based crop stages with a configurable parent template and optional per-age model suffixes. */
        public void customStageBlock(Block block, Identifier parent, String textureKey, IntegerProperty ageProperty, List<Integer> suffixes) {
                this.accept(this.dispatch(block, variant(existingModel(blockName(block) + "_stage0"))).with(PropertyDispatch.modify(ageProperty).generate(age -> {
                        int suffix = suffixes.isEmpty() ? age : suffixes.get(Math.min(suffixes.size() - 1, age));
                        String stageName = blockName(block) + "_stage" + suffix;
                        return modelSwitch(this.emitSingleTextureModel(stageName, parent, textureKey, stageName, "minecraft:cutout"));
                })));
        }

        public void tomatoBlock(Block block, IntegerProperty ageProperty, Property<Boolean> ropeloggedProperty) {
                this.accept(this.dispatch(block, variant(existingModel(blockName(block) + "_stage0"))).with(PropertyDispatch.modify(ageProperty, ropeloggedProperty).generate((age, ropelogged) -> {
                        String stageName = blockName(block) + "_stage" + age;
                        String ropeloggedStageName = blockName(block) + "_old_stage" + age;
                        return modelSwitch(ropelogged
                                        ? this.cropWithRopeModel(ropeloggedStageName, "tomatoes_coiled_rope")
                                        : this.emitSingleTextureModel(stageName, blockTexture("template_crop_cross"), "cross", stageName, "minecraft:cutout"));
                })));
        }

        public void ropedTomatoBlock(Block block, IntegerProperty ageProperty) {
                this.accept(this.dispatch(block, variant(existingModel(blockName(block) + "_stage0"))).with(PropertyDispatch.modify(ageProperty).generate(age -> {
                        String stageName = blockName(block) + "_stage" + age;
                        return modelSwitch(this.cropWithRopeModel(stageName, "tomatoes_coiled_rope"));
                })));
        }

        public void riceRootBlock(Block block) {
                this.accept(this.dispatch(block, variant(existingModel(blockName(block) + "_stage0"))).with(PropertyDispatch.modify(RiceBlock.AGE, RiceBlock.SUPPORTING).generate((age, supporting) -> {
                        String stageName = supporting && age == 3 ? blockName(block) + "_supporting" : blockName(block) + "_stage" + age;
                        return modelSwitch(this.crossModel(stageName, stageName));
                })));
        }

        public void crateBlock(Block block, String cropName) {
                String name = blockName(block);
                this.emitModel(blockModel(block), mcBlock("cube_bottom_top"), null, Map.of(
                                "bottom", blockTexture("crate_bottom"),
                                "side", blockTexture(cropName + "_crate_side"),
                                "top", blockTexture(cropName + "_crate_top")));
                this.simpleBlock(block, blockModel(block));
        }

        public void axisBlock(RotatedPillarBlock block) {
                Identifier horizontal = Identifier.fromNamespaceAndPath(FarmersDelight.MODID,
                                "block/" + blockName(block) + "_horizontal");
                this.accept(this.dispatch((Block) block, variant(blockModel(block))).with(PropertyDispatch.modify(BlockStateProperties.AXIS)
                                .select(Direction.Axis.X, modelSwitch(horizontal).then(X_ROT_90).then(Y_ROT_90))
                                .select(Direction.Axis.Y, NOP)
                                .select(Direction.Axis.Z, modelSwitch(horizontal).then(X_ROT_90))));
        }

        public void organicCompostBlock(Block block) {
                this.accept(this.dispatch(block).with(PropertyDispatch.initial(OrganicCompostBlock.COMPOSTING).generate(composting -> {
                        String textureName = blockName(block) + "_stage" + composting / 2;
                        Identifier modelId = Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + textureName);
                        this.emitModel(modelId, mcBlock("cube_all"), null, Map.of("all", blockTexture(textureName)));
                        return variants(
                                        new Variant(modelId),
                                        new Variant(modelId).withYRot(Quadrant.R90),
                                        new Variant(modelId).withYRot(Quadrant.R180),
                                        new Variant(modelId).withYRot(Quadrant.R270));
                })));
        }

        public void farmlandBlock(Block farmlandBlock, Block dirtBlock) {
                String farmlandName = blockName(farmlandBlock);
                String dirtName = blockName(dirtBlock);
                for (int moisture = 0; moisture <= 7; moisture++) {
                        boolean moist = moisture == 7;
                        String moistSuffix = moist ? "_moist" : "";
                        this.emitModel(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + farmlandName + moistSuffix),
                                        blockTexture("template_farmland_custom"), null, Map.of(
                                                        "bottom", blockTexture(dirtName),
                                                        "side", blockTexture(moist ? farmlandName + "_moist_side" : dirtName),
                                                        "top", blockTexture(farmlandName + moistSuffix)));
                }
                this.accept(this.dispatch(farmlandBlock, variant(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + farmlandName))).with(PropertyDispatch.modify(RichSoilFarmlandBlock.MOISTURE).generate(
                                moisture -> modelSwitch(Identifier.fromNamespaceAndPath(FarmersDelight.MODID,
                                                "block/" + farmlandName + (moisture == 7 ? "_moist" : ""))))));
        }

        public void ropeFenceGateBlock(Block block) {
                String name = blockName(block);
                this.accept(this.dispatch(block, variant(existingModel(name))).with(PropertyDispatch.modify(net.minecraft.world.level.block.FenceGateBlock.IN_WALL, net.minecraft.world.level.block.FenceGateBlock.OPEN, BlockStateProperties.HORIZONTAL_FACING)
                                .generate((Function3<Boolean, Boolean, Direction, VariantMutator>) (inWall, open, facing) -> {
                                        String wallInfix = inWall ? "_wall" : "";
                                        String model = name + wallInfix + (open ? "_open" : "");
                                        return modelSwitch(existingModel(model)).then(yRotFromHorizontal(facing));
                                })));
        }

        public void cabinetBlock(Block block, String woodType) {
                String name = blockName(block);
                for (String suffix : new String[]{"", "_open"}) {
                        this.emitModel(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + name + suffix),
                                        mcBlock("orientable"), null, Map.of(
                                                        "front", blockTexture(woodType + "_cabinet_front" + suffix),
                                                        "side", blockTexture(woodType + "_cabinet_side"),
                                                        "top", blockTexture(woodType + "_cabinet_top")));
                }
                this.accept(this.dispatch(block, variant(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + name)))
                                .with(ROTATION_HORIZONTAL_FACING)
                                .with(PropertyDispatch.modify(CabinetBlock.OPEN).generate(open ->
                                                open ? modelSwitch(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + name + "_open")) : NOP)));
        }

        /** A pie whose whole and sliced models are emitted from the FD pie templates. */
        public void pieBlock(Block block) {
                String name = blockName(block);
                this.emitModel(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + name),
                                blockTexture("template_pie"), null, Map.of(
                                                "bottom", blockTexture("pie_bottom"),
                                                "side", blockTexture("pie_side"),
                                                "top", blockTexture(name + "_top")));
                for (int bites = 1; bites <= 3; bites++) {
                        this.emitModel(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + name + "_slice" + bites),
                                        blockTexture("template_pie_slice" + bites), null, Map.of(
                                                        "bottom", blockTexture("pie_bottom"),
                                                        "side", blockTexture("pie_side"),
                                                        "inner", blockTexture(name + "_inner"),
                                                        "top", blockTexture(name + "_top")));
                }
                this.accept(this.dispatch(block, variant(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + name))).with(PropertyDispatch.modify(PieBlock.BITES, BlockStateProperties.HORIZONTAL_FACING)
                                .generate((BiFunction<Integer, Direction, VariantMutator>) (bites, facing) ->
                                                modelSwitch(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + name + (bites > 0 ? "_slice" + bites : "")))
                                                                .then(yRotFromHorizontalPlus180(facing)))));
        }

        /** A pie whose models are hand-made files. */
        public void customPieBlock(Block block) {
                String name = blockName(block);
                this.accept(this.dispatch(block, variant(existingModel(name))).with(PropertyDispatch.modify(PieBlock.BITES, BlockStateProperties.HORIZONTAL_FACING)
                                .generate((BiFunction<Integer, Direction, VariantMutator>) (bites, facing) ->
                                                modelSwitch(existingModel(name + (bites > 0 ? "_slice" + bites : "")))
                                                                .then(yRotFromHorizontalPlus180(facing)))));
        }

        public void feastBlock(FeastBlock block) {
                String name = blockName(block);
                this.accept(this.dispatch(block, variant(existingModel(name))).with(PropertyDispatch.modify(block.getServingsProperty(), FeastBlock.FACING)
                                .generate((BiFunction<Integer, Direction, VariantMutator>) (servings, facing) -> {
                                        String suffix = "_stage" + (block.getMaxServings() - servings);
                                        if (servings == 0) {
                                                suffix = block.hasLeftovers ? "_leftovers" : "_stage" + (block.getServingsProperty().getPossibleValues().size() - 2);
                                        }
                                        return modelSwitch(existingModel(name + suffix)).then(yRotFromHorizontalPlus180(facing));
                                })));
        }

        public void wildCropBlock(Block block, boolean isBushCrop) {
                String name = blockName(block);
                Identifier model;
                if (isBushCrop) {
                        model = this.emitSingleTextureModel(name, blockTexture("template_bush_crop"), "crop", name, "minecraft:cutout");
                } else {
                        model = this.crossModel(name, name);
                }
                this.simpleBlock(block, model);
        }

        public void doublePlantBlock(Block block) {
                String name = blockName(block);
                this.accept(this.dispatch(block, variant(existingModel(name))).with(PropertyDispatch.modify(net.minecraft.world.level.block.DoublePlantBlock.HALF).generate(half ->
                                modelSwitch(this.crossModel(name + (half == DoubleBlockHalf.LOWER ? "_bottom" : "_top"),
                                                name + (half == DoubleBlockHalf.LOWER ? "_bottom" : "_top"))))));
        }

        public void cookingPotBlock(Block block) {
                String name = blockName(block);
                this.accept(this.dispatch(block, variant(existingModel(name)))
                                .with(ROTATION_HORIZONTAL_FACING)
                                .with(PropertyDispatch.modify(CookingPotBlock.SUPPORT).generate(support -> switch (support) {
                                        case NONE -> NOP;
                                        case TRAY -> modelSwitch(existingModel(name + "_tray"));
                                        case HANDLE -> modelSwitch(existingModel(name + "_handle"));
                                })));
        }

        public void skilletBlock(Block block) {
                String name = blockName(block);
                this.accept(this.dispatch(block, variant(existingModel(name)))
                                .with(ROTATION_HORIZONTAL_FACING)
                                .with(PropertyDispatch.modify(SkilletBlock.SUPPORT).generate(support ->
                                                support ? modelSwitch(existingModel(name + "_tray")) : NOP)));
        }

        public void stoveBlock(Block block) {
                String name = blockName(block);
                for (String suffix : new String[]{"", "_on"}) {
                        this.emitModel(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + name + suffix),
                                        mcBlock("orientable_with_bottom"), null, Map.of(
                                                        "bottom", blockTexture(name + "_bottom"),
                                                        "front", blockTexture(name + "_front" + suffix),
                                                        "side", blockTexture(name + "_side"),
                                                        "top", blockTexture(name + "_top" + suffix)));
                }
                this.accept(this.dispatch(block, variant(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + name)))
                                .with(ROTATION_HORIZONTAL_FACING)
                                .with(PropertyDispatch.modify(StoveBlock.LIT).generate(lit ->
                                                lit ? modelSwitch(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + name + "_on")) : NOP)));
        }

        // Model helpers ------------------------------

        /** Emits a cutout cross model and returns its id. */
        private Identifier crossModel(String modelName, String textureName) {
                return this.emitSingleTextureModel(modelName, mcBlock("cross"), "cross", textureName, "minecraft:cutout");
        }

        /** Emits a model with a single custom-named texture slot. */
        private Identifier emitSingleTextureModel(String modelName, Identifier parent, String textureKey, String textureName, @Nullable String renderType) {
                Identifier id = Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + modelName);
                this.emitModel(id, parent, renderType, Map.of(textureKey, blockTexture(textureName)));
                return id;
        }

        private Identifier modelBasket(String baseName) {
                Identifier id = Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + baseName);
                this.emitModel(id, blockTexture("template_basket"), null, Map.of(
                                "bottom", blockTexture(baseName + "_bottom"),
                                "side", blockTexture(baseName + "_side"),
                                "top", blockTexture(baseName + "_top"),
                                "handle", blockTexture(baseName + "_handle")));
                return id;
        }

        private Identifier modelCubeBottomTop(String baseName) {
                Identifier id = Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + baseName);
                this.emitModel(id, mcBlock("cube_bottom_top"), null, Map.of(
                                "bottom", blockTexture(baseName + "_bottom"),
                                "side", blockTexture(baseName + "_side"),
                                "top", blockTexture(baseName + "_top")));
                return id;
        }

        private Identifier cropWithRopeModel(String baseName, String ropeSideTextureName) {
                Identifier id = Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "block/" + baseName);
                this.emitModel(id, blockTexture("template_crop_with_rope"), "cutout", Map.of(
                                "crop", blockTexture(baseName),
                                "rope_side", blockTexture(ropeSideTextureName),
                                "rope_top", blockTexture("rope_top")));
                return id;
        }
}
