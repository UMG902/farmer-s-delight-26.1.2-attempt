package vectorwing.farmersdelight.common.accessor;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.gen.Accessor;

public interface VillagerProfessionAccessor {
    @Accessor("secondaryPoi")
    ImmutableSet<Block> farmersdelight$getSecondaryPoi();

    @Accessor("secondaryPoi")
    void farmersdelight$setSecondaryPoi(ImmutableSet<Block> secondaryPoi);
}
