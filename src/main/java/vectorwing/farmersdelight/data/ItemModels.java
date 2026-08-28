package vectorwing.farmersdelight.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.DataProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

/**
 * Client item definitions are checked in under src/generated/resources.
 * They are copied to the main resource set by the port build so the 26.1
 * ModelManager always sees assets/<modid>/items/*.json in dev runs.
 */
public final class ItemModels implements DataProvider {
    private final PackOutput output;

    public ItemModels(PackOutput output, Object ignored) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Farmer's Delight Item Models";
    }
}
