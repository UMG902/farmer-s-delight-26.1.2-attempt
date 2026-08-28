package vectorwing.farmersdelight.common.block.entity.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;

/**
 * Uses the given item as a tool when facing a Cutting Board.
 * Stubbed - OptionalDispenseItemBehavior removed in NeoForge 26.1.2.99.
 */
@ParametersAreNonnullByDefault
public class CuttingBoardDispenseBehavior implements DispenseItemBehavior
{
	private static final HashMap<Item, DispenseItemBehavior> DISPENSE_ITEM_BEHAVIOR_HASH_MAP = new HashMap<>();
	public static final CuttingBoardDispenseBehavior INSTANCE = new CuttingBoardDispenseBehavior();

	public static void registerBehaviour(Item item, CuttingBoardDispenseBehavior behavior) {
		DISPENSE_ITEM_BEHAVIOR_HASH_MAP.put(item, DispenserBlock.DISPENSER_REGISTRY.get(item));
		DispenserBlock.registerBehavior(item, behavior);
	}

	@Override
	public final ItemStack dispense(BlockSource source, ItemStack stack) {
		if (tryDispenseStackOnCuttingBoard(source, stack)) {
			source.level().levelEvent(1000, source.pos(), 0);
			return stack;
		}
		DispenseItemBehavior old = DISPENSE_ITEM_BEHAVIOR_HASH_MAP.get(stack.getItem());
		if (old != null) {
			return old.dispense(source, stack);
		}
		return stack;
	}

	public boolean tryDispenseStackOnCuttingBoard(BlockSource source, ItemStack stack) {
		Level level = source.level();
		BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof CuttingBoardBlock && level.getBlockEntity(pos) instanceof CuttingBoardBlockEntity cuttingBoard) {
			if (!cuttingBoard.isEmpty() && cuttingBoard.processStoredItemUsingTool(stack, null)) {
				return true;
			}
			return true;
		}
		return false;
	}
}
