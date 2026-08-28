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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

/** Renders the item stored on a Cutting Board using the 26.1 item-model pipeline. */
public class CuttingBoardRenderer implements BlockEntityRenderer<CuttingBoardBlockEntity, CuttingBoardRenderer.State> {
    private final net.minecraft.client.renderer.item.ItemModelResolver itemModelResolver;

    public CuttingBoardRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public boolean hasItem;
        public float yRotation;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CuttingBoardBlockEntity blockEntity, State state, float partialTick, Vec3 cameraPosition, @Nullable net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        state.extractBase(blockEntity, state, breakProgress);

        ItemStack stack = blockEntity.getStoredItem();
        state.hasItem = !stack.isEmpty() && !blockEntity.isItemCarvingBoard();
        state.yRotation = -blockEntity.getBlockState().getValue(CuttingBoardBlock.FACING).toYRot();

        if (state.hasItem) {
            Level level = blockEntity.getLevel();
            this.itemModelResolver.updateForTopItem(
                state.item,
                stack,
                ItemDisplayContext.FIXED,
                level,
                null,
                (int) blockEntity.getBlockPos().asLong()
            );
        } else {
            state.item.clear();
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (!state.hasItem || state.item.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.13D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.6F, 0.6F, 0.6F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
