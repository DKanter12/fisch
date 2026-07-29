package com.fisch.screen;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;

public class RodSlot extends Slot {
    public RodSlot(net.minecraft.world.Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof FishingRodItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
