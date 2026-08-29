package vectorwing.farmersdelight.common.event;

/*
 * Villager trades have been migrated to the data-driven registry system
 * introduced in NeoForge 26.1 / Minecraft 1.21.11+.
 *
 * The previous API (VillagerTradesEvent, WandererTradesEvent, the
 * VillagerTrades.ItemListing interface, and the BasicItemListing helper
 * class) has been REMOVED in 26.1. Trades are now declared as JSON files in:
 *
 *   data/<namespace>/villager_trade/<path>.json
 *   data/<namespace>/tags/villager_trade/<path>.json
 *
 * Per the NeoForge 26.1 primer section "Datapack Villager Trades":
 *   - "wants" = the stack the trader WANTS (i.e. what the player gives)
 *   - "gives" = the stack the trader GIVES (i.e. what the player receives)
 *
 * This class is intentionally left empty so existing references to the class
 * still resolve. The trades previously wired up here (farmer buying FD crops,
 * wandering trader selling FD seeds) are now defined by the JSON files at:
 *
 *   src/generated/resources/data/farmersdelight/villager_trade/farmer/1/*.json
 *   src/generated/resources/data/farmersdelight/villager_trade/farmer/2/*.json
 *   src/generated/resources/data/farmersdelight/villager_trade/wandering_trader/*.json
 *
 * Tag wiring (which adds these trades to the farmer and wandering_trader
 * trade lists) is at:
 *
 *   src/generated/resources/data/minecraft/tags/villager_trade/farmer/level_1.json
 *   src/generated/resources/data/minecraft/tags/villager_trade/farmer/level_2.json
 *   src/generated/resources/data/minecraft/tags/villager_trade/wandering_trader/common.json
 *
 * NOTE: The upstream Fabric refabricated repo (FarmersDelightRefabricated) has
 * the wandering-trader wants/gives direction inverted (the wandering trader
 * BUYS FD items from the player instead of SELLING them). This NeoForge port
 * corrects that bug to match the original vectorwing/FarmersDelight@1.21
 * behavior, where the wandering trader SELLS FD items (player gives 1
 * emerald, receives 1 FD item).
 *
 * The previous Configuration.ENABLE_FARMERS_BUY_FD_CROPS and
 * Configuration.ENABLE_WANDERING_TRADER_SELLS_FD_ITEMS config flags no
 * longer have any effect on these data-driven trades. To disable a trade,
 * remove or comment out its entry in the tag JSON.
 */
final class VillagerEvents {
    private VillagerEvents() {
        // Utility class, no instantiation.
    }
}
