package vectorwing.farmersdelight.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import vectorwing.farmersdelight.common.utility.TextUtils;

/**
 * Stubbed - GuiGraphics removed in MC 1.21.5. ClientTooltipComponent.renderImage signature changed.
 * Needs migration to new tooltip rendering API.
 */
public class CookingPotTooltip implements ClientTooltipComponent
{
	private static final int ITEM_SIZE = 16;
	private static final int MARGIN = 4;

	private final int textSpacing = Minecraft.getInstance().font.lineHeight + 1;
	private final ItemStack mealStack;

	public CookingPotTooltip(CookingPotTooltipComponent tooltip) {
		this.mealStack = tooltip.mealStack;
	}

	@Override
	public int getHeight(Font font) {
		return mealStack.isEmpty() ? textSpacing : textSpacing + ITEM_SIZE;
	}

	@Override
	public int getWidth(Font font) {
		if (!mealStack.isEmpty()) {
			MutableComponent textServingsOf = mealStack.getCount() == 1
					? TextUtils.tooltip("cooking_pot.single_serving")
					: TextUtils.tooltip("cooking_pot.many_servings", mealStack.getCount());
			return Math.max(font.width(textServingsOf), font.width(mealStack.getHoverName()) + 20);
		} else {
			return font.width(TextUtils.tooltip("cooking_pot.empty"));
		}
	}


	public record CookingPotTooltipComponent(ItemStack mealStack) implements TooltipComponent
	{
	}
}
