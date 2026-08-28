package vectorwing.farmersdelight.integration.jei;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import net.minecraft.world.item.crafting.RecipeMap;

@EventBusSubscriber(modid = FarmersDelight.MODID)
public final class RecipeSyncEvents {
    private RecipeSyncEvents() {}

    @SubscribeEvent
    public static void syncRecipes(OnDatapackSyncEvent event) {
        event.sendRecipes(ModRecipeTypes.COOKING.get(), ModRecipeTypes.CUTTING.get());
    }

    @EventBusSubscriber(modid = FarmersDelight.MODID, value = Dist.CLIENT)
    public static final class Client {
        private Client() {}

        @SubscribeEvent
        public static void recipesReceived(RecipesReceivedEvent event) {
            RecipeMap map = event.getRecipeMap();
            if (!map.values().isEmpty()) {
                FDRecipes.setRecipeMap(map);
            }
        }

        @SubscribeEvent
        public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            FDRecipes.setRecipeMap(RecipeMap.EMPTY);
        }
    }
}
