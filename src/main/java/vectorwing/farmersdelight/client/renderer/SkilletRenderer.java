package vectorwing.farmersdelight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;

public class SkilletRenderer implements BlockEntityRenderer<SkilletBlockEntity, BlockEntityRenderState> {
    public SkilletRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public BlockEntityRenderState createRenderState() { return new BlockEntityRenderState(); }
    @Override public void extractRenderState(SkilletBlockEntity blockEntity, BlockEntityRenderState state, float partialTick, Vec3 cameraPosition, @Nullable net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breakProgress) {  }
    @Override public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {}
}
