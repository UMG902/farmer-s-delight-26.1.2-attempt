package vectorwing.farmersdelight.client.renderer;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.StandingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.Nullable;
import net.minecraft.resources.Identifier;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.state.CanvasSign;

/** Farmer's Delight canvas standing/wall sign renderer for 26.1.x. */
public class CanvasSignRenderer extends StandingSignRenderer {
    private @Nullable DyeColor currentBackgroundColor;

    public CanvasSignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public StandingSignRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SignBlockEntity blockEntity, StandingSignRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        if (state instanceof State canvasState) {
            canvasState.backgroundColor = blockEntity.getBlockState().getBlock() instanceof CanvasSign canvas
                    ? canvas.getBackgroundColor()
                    : null;
        }
    }

    @Override
    public void submit(StandingSignRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        currentBackgroundColor = state instanceof State canvasState ? canvasState.backgroundColor : null;
        try {
            super.submit(state, poseStack, submitNodeCollector, camera);
        } finally {
            currentBackgroundColor = null;
        }
    }

    @Override
    protected SpriteId getSignSprite(net.minecraft.world.level.block.state.properties.WoodType woodType) {
        String suffix = currentBackgroundColor == null ? "" : "_" + currentBackgroundColor.getName();
        return new SpriteId(
                Sheets.SIGN_SHEET,
                Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "entity/signs/canvas" + suffix)
        );
    }

    private static final class State extends StandingSignRenderState {
        private @Nullable DyeColor backgroundColor;
    }
}
