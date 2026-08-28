package vectorwing.farmersdelight.common.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.registry.ModBlocks;

/**
 * Stubbed - BlockRenderDispatcher.renderBreakingTexture signature and ModelData changed in NeoForge 26.1.2.99.
 * The mixin target method signature needs to be updated to match the new API.
 */
@Mixin(targets = "net.minecraft.client.renderer.block.BlockRenderDispatcher")
public abstract class HideBlockBreakProgressMixin
{
	// Stubbed - method signature for renderBreakingTexture changed (ModelData parameter removed)
}
