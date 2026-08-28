package vectorwing.farmersdelight.common.mixin;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import vectorwing.farmersdelight.common.accessor.VillagerProfessionAccessor;

@Mixin(VillagerProfession.class)
public abstract class VillagerProfessionMixin implements VillagerProfessionAccessor {
    @Shadow @Final @Mutable
    private ImmutableSet<Block> secondaryPoi;

    @Override
    public ImmutableSet<Block> farmersdelight$getSecondaryPoi() {
        return this.secondaryPoi;
    }

    @Override
    public void farmersdelight$setSecondaryPoi(ImmutableSet<Block> secondaryPoi) {
        this.secondaryPoi = secondaryPoi;
    }
}
