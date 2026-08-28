package vectorwing.farmersdelight.data.tools;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Structure updater is disabled for the 26.1 port. Existing structures are left untouched
 * during datagen; this class remains as a compatibility provider for callers from older FD code.
 */
public class StructureUpdater implements DataProvider {
    private final String basePath;

    public StructureUpdater(String basePath, String modid, PackOutput output) {
        this.basePath = basePath;
    }

    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput output) {
        return CompletableFuture.completedFuture(null);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Update structure files in " + basePath;
    }
}
