package vectorwing.farmersdelight.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.joml.Vector3f;

/**
 * Stubbed - GuiGraphics removed and AbstractSignEditScreen API changed in MC 1.21.5.
 * Needs migration to new rendering and sign edit screen API.
 */
public class HangingCanvasSignEditScreen extends net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen
{
	public HangingCanvasSignEditScreen(SignBlockEntity signBlockEntity, boolean isFrontText, boolean isTextFilteringEnabled) {
		super(signBlockEntity, isFrontText, isTextFilteringEnabled, Component.translatable("hanging_sign.edit"));
	}

	@Override
	protected float getSignYOffset() {
		return 0.0F;
	}

	@Override
	protected Vector3f getSignTextScale() {
		return new Vector3f(1.0F, 1.0F, 1.0F);
	}
	@Override
	protected void extractSignBackground(net.minecraft.client.gui.GuiGraphicsExtractor graphics) {
	}
}
