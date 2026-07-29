package com.fisch.screen;

import com.fisch.fish.Relic;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RelicSlot extends Slot {
    public RelicSlot(net.minecraft.world.Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof Relic;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
