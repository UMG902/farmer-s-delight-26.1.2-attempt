package vectorwing.farmersdelight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.SkilletBlock;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;

import java.util.Random;

/** Renders the ingredient sitting in a placed skillet using the 26.1 item-model pipeline. */
public class SkilletRenderer implements BlockEntityRenderer<SkilletBlockEntity, SkilletRenderer.State> {
    private final net.minecraft.client.renderer.item.ItemModelResolver itemModelResolver;
    private final Random random = new Random();

    public SkilletRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    public static class State extends BlockEntityRenderState {
        private static final int MAX_RENDERED_ITEMS = 5;
        public final ItemStackRenderState[] items = new ItemStackRenderState[MAX_RENDERED_ITEMS];
        public int itemCount;
        public float yRotation;
        public int seed;

        public State() {
            for (int i = 0; i < items.length; i++) {
                items[i] = new ItemStackRenderState();
            }
        }
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SkilletBlockEntity blockEntity, State state, float partialTick, Vec3 cameraPosition,
                                   @Nullable net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        state.extractBase(blockEntity, state, breakProgress);

        ItemStack stack = blockEntity.getStoredStack();
        if (stack.isEmpty()) {
            state.itemCount = 0;
            for (ItemStackRenderState item : state.items) item.clear();
            return;
        }

        state.itemCount = Math.min(State.MAX_RENDERED_ITEMS, getModelCount(stack));
        state.seed = Item.getId(stack.getItem()) + stack.getDamageValue();
        state.yRotation = -blockEntity.getBlockState().getValue(SkilletBlock.FACING).toYRot();

        Level level = blockEntity.getLevel();
        for (int i = 0; i < state.itemCount; i++) {
            itemModelResolver.updateForTopItem(
                    state.items[i], stack,
                    ItemDisplayContext.FIXED,
                    level,
                    null,
                    state.seed + i
            );
        }
        for (int i = state.itemCount; i < state.items.length; i++) {
            state.items[i].clear();
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.itemCount == 0) return;

        random.setSeed(state.seed);
        for (int i = 0; i < state.itemCount; i++) {
            ItemStackRenderState item = state.items[i];
            if (item.isEmpty()) continue;

            float xOffset = (random.nextFloat() * 2.0F - 1.0F) * 0.075F;
            float zOffset = (random.nextFloat() * 2.0F - 1.0F) * 0.075F;

            poseStack.pushPose();
            poseStack.translate(0.5D + xOffset, 0.1D + 0.03D * (i + 1), 0.5D + zOffset);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yRotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    private static int getModelCount(ItemStack stack) {
        int modelCount = 1;
        if (stack.getCount() > 1) {
            modelCount += Mth.ceil(((float) stack.getCount() / stack.getMaxStackSize()) * 4.0F);
        }
        return modelCount;
    }
}
