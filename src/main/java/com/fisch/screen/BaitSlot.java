package com.fisch.screen;

import com.fisch.item.Bait;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BaitSlot
        extends Slot {

    public BaitSlot(
            RodBaitContainer container,
            int x,
            int y
    ) {

        super(
                container,
                0,
                x,
                y
        );
    }

    @Override
    public boolean mayPlace(
            ItemStack stack
    ) {

        return stack.getItem()
                instanceof Bait;
    }

    @Override
    public int getMaxStackSize() {

        return 64;
    }
}