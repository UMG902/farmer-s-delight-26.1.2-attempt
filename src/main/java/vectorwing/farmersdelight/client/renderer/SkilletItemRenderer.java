package vectorwing.farmersdelight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.item.SkilletItem;
import vectorwing.farmersdelight.common.item.component.ItemStackWrapper;
import vectorwing.farmersdelight.common.registry.ModDataComponents;

import java.util.function.Consumer;

/** Dynamic item rendering for the ingredient held inside a skillet while it is being used. */
public class SkilletItemRenderer implements SpecialModelRenderer<SkilletItemRenderer.RenderData> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "skillet_cooking");

    private final ItemModelResolver itemModelResolver;

    public SkilletItemRenderer(ItemModelResolver itemModelResolver) {
        this.itemModelResolver = itemModelResolver;
    }

    public record RenderData(ItemStack ingredient, long flipTimestamp, boolean flipped) {}

    @Override
    public @Nullable RenderData extractArgument(ItemStack stack) {
        ItemStackWrapper wrapper = stack.getOrDefault(ModDataComponents.SKILLET_INGREDIENT.get(), ItemStackWrapper.EMPTY);
        ItemStack ingredient = wrapper.getStack();
        if (ingredient.isEmpty()) return null;

        long timestamp = stack.getOrDefault(ModDataComponents.SKILLET_FLIP_TIMESTAMP.get(), -1L);
        boolean flipped = stack.getOrDefault(ModDataComponents.SKILLET_FLIPPED.get(), false);
        return new RenderData(ingredient.copy(), timestamp, flipped);
    }

    @Override
    public void submit(@Nullable RenderData data, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (data == null || data.ingredient().isEmpty()) return;

        ItemStackRenderState ingredientState = new ItemStackRenderState();
        Minecraft mc = Minecraft.getInstance();
        itemModelResolver.updateForTopItem(
                ingredientState, data.ingredient(), ItemDisplayContext.FIXED, mc.level, null,
                Item.getId(data.ingredient().getItem())
        );
        if (ingredientState.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.0D / 16.0D, 0.5D);

        if (data.flipTimestamp() >= 0L && mc.level != null) {
            float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float animation = ((mc.level.getGameTime() - data.flipTimestamp()) + partialTicks) / SkilletItem.FLIP_TIME;
            animation = Mth.clamp(animation, 0.0F, 1.0F);
            poseStack.translate(0.0D, 0.4D * Mth.sin(animation * Mth.PI), 0.0D);
            float rotationAnimation = data.flipped() ? animation + 1.0F : animation;
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F * rotationAnimation));
        } else if (data.flipped()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ingredientState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, outlineColor);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        // The ingredient remains inside the skillet's normal item bounds.
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<RenderData> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public @Nullable SpecialModelRenderer<RenderData> bake(SpecialModelRenderer.BakingContext context) {
            return new SkilletItemRenderer(Minecraft.getInstance().getItemModelResolver());
        }
    }

    public static class ArmPoseTransformer implements IArmPoseTransformer {
        @Override
        public void applyTransform(HumanoidModel<?> model, HumanoidRenderState state, net.minecraft.world.entity.HumanoidArm arm) {
            ItemStack stack = Minecraft.getInstance().player != null
                    ? Minecraft.getInstance().player.getUseItem() : ItemStack.EMPTY;
            if (!(stack.getItem() instanceof SkilletItem) || !stack.has(ModDataComponents.SKILLET_INGREDIENT.get())) return;

            Minecraft mc = Minecraft.getInstance();
            float rotation = (float) Math.toRadians(-90.0D);

            if (stack.has(ModDataComponents.SKILLET_FLIP_TIMESTAMP.get()) && mc.level != null) {
                long timestamp = stack.get(ModDataComponents.SKILLET_FLIP_TIMESTAMP.get());
                float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
                float animation = ((mc.level.getGameTime() - timestamp) + partialTicks) / SkilletItem.FLIP_TIME;
                animation = Mth.clamp(animation, 0.0F, 1.0F);
                rotation += (-Mth.sin(animation * Mth.TWO_PI) * 15.0F - 20.0F) * (float) (Math.PI / 180.0D);
            }

            if (arm == net.minecraft.world.entity.HumanoidArm.LEFT) model.leftArm.xRot = rotation;
            else model.rightArm.xRot = rotation;
        }
    }
}
