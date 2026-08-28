package vectorwing.farmersdelight.common.block.entity.inventory;

import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Legacy IItemHandler bridge for basket inventories. */
public class BasketInvWrapper implements IItemHandler {
    private final Container basket;
    public BasketInvWrapper(Container basket) { this.basket = basket; }
    @Override public int getSlots() { return basket.getContainerSize(); }
    @Override public ItemStack getStackInSlot(int slot) { return basket.getItem(slot); }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) return stack;
        ItemStack existing = basket.getItem(slot);
        int limit = getSlotLimit(slot);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) return stack;
        int move = Math.min(stack.getCount(), limit - existing.getCount());
        if (move <= 0) return stack;
        if (!simulate) { ItemStack copy = stack.copy(); copy.setCount(existing.getCount()+move); basket.setItem(slot, copy); basket.setChanged(); }
        ItemStack rem = stack.copy(); rem.shrink(move); return rem;
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack existing = basket.getItem(slot);
        if (existing.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        int move = Math.min(amount, existing.getCount());
        ItemStack out = existing.copy(); out.setCount(move);
        if (!simulate) { existing.shrink(move); basket.setItem(slot, existing); basket.setChanged(); }
        return out;
    }
    @Override public int getSlotLimit(int slot) { return 64; }
    @Override public boolean isItemValid(int slot, ItemStack stack) { return basket.canPlaceItem(slot, stack); }
}
