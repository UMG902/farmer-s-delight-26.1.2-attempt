package vectorwing.farmersdelight.client.renderer;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.item.component.ItemStackWrapper;
import vectorwing.farmersdelight.common.registry.ModDataComponents;

/**
 * Replaces Farmer's Delight's 1.21 `ItemProperties` cooking predicate on NeoForge 26.1.
 */
public record SkilletCookingProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<SkilletCookingProperty> MAP_CODEC = MapCodec.unit(new SkilletCookingProperty());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        return !stack.getOrDefault(ModDataComponents.SKILLET_INGREDIENT.get(), ItemStackWrapper.EMPTY).getStack().isEmpty();
    }

    @Override
    public MapCodec<SkilletCookingProperty> type() {
        return MAP_CODEC;
    }
}
