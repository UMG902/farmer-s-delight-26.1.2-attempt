package vectorwing.farmersdelight.data;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.CachedOutput;
import java.util.concurrent.CompletableFuture;

/**
 * Stubbed - old datagen API (BlockStateProvider, ModelFile, ConfiguredModel, ExistingFileHelper) no longer exists in NeoForge 26.1.2.99.
 */
public class BlockStates implements DataProvider {
    private final PackOutput output;

    public BlockStates(PackOutput output, Object existingFileHelper) {
        this.output = output;
    }

    @Override
    public String getName() {
        return "Farmers Delight Block States (stubbed)";
    }
	@Override public CompletableFuture<?> run(CachedOutput output) { return CompletableFuture.completedFuture(null); }

}
