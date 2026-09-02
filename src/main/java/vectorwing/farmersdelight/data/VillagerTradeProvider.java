package vectorwing.farmersdelight.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

/**
 * Emits the hand-maintained villager trade JSONs (new data-driven registry in 26.1) so they
 * have a datagen owner in src/generated/resources. The file contents mirror the checked-in
 * resources exactly; edit either this provider or the JSONs, never both.
 */
public class VillagerTradeProvider implements DataProvider
{
	private final PackOutput.PathProvider pathProvider;
	private final PackOutput.PathProvider tagPathProvider;

	public VillagerTradeProvider(PackOutput output) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "villager_trade");
		this.tagPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/villager_trade");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		// Farmer trades: crops for emeralds
		trade(cache, "farmer/1/tomato_emerald", item("farmersdelight:tomato", 26.0), item("minecraft:emerald", null), 16.0, 2.0);
		trade(cache, "farmer/1/onion_emerald", item("farmersdelight:onion", 26.0), item("minecraft:emerald", null), 16.0, 2.0);
		trade(cache, "farmer/2/cabbage_emerald", item("farmersdelight:cabbage", 16.0), item("minecraft:emerald", null), 16.0, 5.0);
		trade(cache, "farmer/2/rice_emerald", item("farmersdelight:rice", 20.0), item("minecraft:emerald", null), 16.0, 5.0);
		// Wandering trader: emeralds for crops and seeds
		trade(cache, "wandering_trader/onion_emerald", item("minecraft:emerald", 1.0), item("farmersdelight:onion", null), 12.0, 12.0);
		trade(cache, "wandering_trader/tomato_emerald", item("minecraft:emerald", 1.0), item("farmersdelight:tomato_seeds", null), 12.0, 12.0);
		trade(cache, "wandering_trader/cabbage_emerald", item("minecraft:emerald", 1.0), item("farmersdelight:cabbage_seeds", null), 12.0, 12.0);
		trade(cache, "wandering_trader/rice_emerald", item("minecraft:emerald", 1.0), item("farmersdelight:rice", null), 12.0, 12.0);
		// Tags that hook the trades into the vanilla trade sets (optional references)
		tradeTag(cache, "farmer/level_1", "farmer/1/onion_emerald", "farmer/1/tomato_emerald");
		tradeTag(cache, "farmer/level_2", "farmer/2/cabbage_emerald", "farmer/2/rice_emerald");
		tradeTag(cache, "wandering_trader/common", "wandering_trader/cabbage_emerald", "wandering_trader/rice_emerald",
				"wandering_trader/tomato_emerald", "wandering_trader/onion_emerald");
		return CompletableFuture.allOf();
	}

	private void tradeTag(CachedOutput cache, String tagPath, String... tradePaths) {
		JsonObject json = new JsonObject();
		JsonArray values = new JsonArray();
		for (String tradePath : tradePaths) {
			JsonObject entry = new JsonObject();
			entry.addProperty("id", "farmersdelight:" + tradePath);
			entry.addProperty("required", false);
			values.add(entry);
		}
		json.add("values", values);
		DataProvider.saveStable(cache, json, this.tagPathProvider.json(net.minecraft.resources.Identifier.parse("minecraft:" + tagPath)));
	}

	private void trade(CachedOutput cache, String path, JsonObject wants, JsonObject gives, double maxUses, double xp) {
		JsonObject json = new JsonObject();
		json.add("wants", wants);
		json.add("gives", gives);
		json.addProperty("max_uses", maxUses);
		json.addProperty("reputation_discount", 0.05);
		json.addProperty("xp", xp);
		DataProvider.saveStable(cache, json, this.pathProvider.json(net.minecraft.resources.Identifier.parse("farmersdelight:" + path)));
	}

	private static JsonObject item(String id, Double count) {
		JsonObject json = new JsonObject();
		json.addProperty("id", id);
		if (count != null) {
			json.addProperty("count", count);
		}
		return json;
	}

	@Override
	public String getName() {
		return "Farmer's Delight Villager Trades";
	}
}
