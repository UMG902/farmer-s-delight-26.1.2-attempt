package vectorwing.farmersdelight.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import vectorwing.farmersdelight.client.gui.state.GuiCanvasSignRenderState;
import vectorwing.farmersdelight.common.block.state.CanvasSign;

import javax.annotation.Nullable;

/**
 * Canvas Sign edit screen for NeoForge 26.1.x.
 * Renders the standing/wall canvas-sign GUI preview by submitting a
 * {@link GuiCanvasSignRenderState} picture-in-picture render state, which
 * {@link vectorwing.farmersdelight.client.gui.renderer.GuiCanvasSignRenderer}
 * draws using the canvas sign sprite from the sign atlas.
 *
 * <p>Replaces the previous stub that inherited {@code super.extractSignBackground}
 * and rendered the vanilla spruce sign model instead of the canvas sign.</p>
 */
public class CanvasSignEditScreen extends SignEditScreen {
	@Nullable
	protected Model.Simple signModel;
	@Nullable
	protected DyeColor dye;
	protected final boolean isFrontText;

	public CanvasSignEditScreen(SignBlockEntity signBlockEntity, boolean isFront, boolean isTextFilteringEnabled) {
		super(signBlockEntity, isFront, isTextFilteringEnabled);
		Block block = signBlockEntity.getBlockState().getBlock();
		if (block instanceof CanvasSign canvasSign) {
			this.dye = canvasSign.getBackgroundColor();
		}
		this.isFrontText = isFront;
	}

	@Override
	protected void init() {
		super.init();
		PlainSignBlock.Attachment bl = this.sign.getBlockState().getBlock() instanceof WallSignBlock
                ? PlainSignBlock.Attachment.WALL
                : PlainSignBlock.Attachment.GROUND;
		this.signModel = StandingSignRenderer.createSignModel(this.minecraft.getEntityModels(), this.woodType, bl);
	}

	@Override
	protected void extractSignBackground(GuiGraphicsExtractor guiGraphics) {
		if (this.signModel != null) {
			int i = this.width / 2;
			int j = i - 48;
			int l = i + 48;
			guiGraphics.submitPictureInPictureRenderState(
					new GuiCanvasSignRenderState(
							signModel, dye, j, 66, l, 168, 62.500004F,
							guiGraphics.peekScissorStack()));
		}
	}
}
