package vectorwing.farmersdelight.common.registry;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import vectorwing.farmersdelight.FarmersDelight;

/**
 * Custom ingredient registrations are disabled in the 26.1 port; in-code ability
 * ingredients are converted to vanilla ingredients before recipe serialization.
 */
public final class ModIngredientTypes {
    private ModIngredientTypes() {}
    public static final DeferredRegister<?> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, FarmersDelight.MODID);
}
