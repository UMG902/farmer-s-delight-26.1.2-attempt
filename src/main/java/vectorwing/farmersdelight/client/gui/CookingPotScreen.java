package vectorwing.farmersdelight.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private static final Identifier BACKGROUND_TEXTURE =
            Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "textures/gui/cooking_pot.png");

    public CookingPotScreen(CookingPotMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void init() {
        super.init();
        this.titleLabelX = 28;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, this.leftPos + 176, this.topPos + 166, 0.0f, 176.0f / 256.0f, 0.0f, 166.0f / 256.0f);

        if (this.menu.isHeated()) {
            graphics.blit(BACKGROUND_TEXTURE, this.leftPos + 47, this.topPos + 55, this.leftPos + 64, this.topPos + 70, 176.0f / 256.0f, 193.0f / 256.0f, 0.0f, 15.0f / 256.0f);
        }

        int progress = this.menu.getCookProgressionScaled();
        graphics.blit(BACKGROUND_TEXTURE, this.leftPos + 89, this.topPos + 25, this.leftPos + 89 + progress + 1, this.topPos + 42, 176.0f / 256.0f, (176.0f + progress + 1.0f) / 256.0f, 15.0f / 256.0f, 32.0f / 256.0f);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
    }
}
