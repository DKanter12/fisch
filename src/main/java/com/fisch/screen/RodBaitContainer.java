package com.fisch.screen;

import com.fisch.item.Bait;
import com.fisch.rod.RodBaitData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RodBaitContainer
        implements Container {

    private final BaitScreenHandler menu;

    public RodBaitContainer(
            BaitScreenHandler menu
    ) {
        this.menu = menu;
    }

    private ItemStack getRod() {

        return menu.getRod();
    }

    @Override
    public int getContainerSize() {

        return 1;
    }

    @Override
    public ItemStack getItem(
            int slot
    ) {

        if (slot != 0) {
            return ItemStack.EMPTY;
        }

        return RodBaitData.getBait(
                getRod()
        );
    }

    @Override
    public void setItem(
            int slot,
            ItemStack stack
    ) {

        if (slot != 0) {
            return;
        }

        if (stack.isEmpty()) {

            RodBaitData.removeBait(
                    getRod()
            );

        } else {

            ItemStack bait =
                    stack.copy();

            bait.setCount(
                    Math.min(
                            bait.getCount(),
                            64
                    )
            );

            RodBaitData.setBait(
                    getRod(),
                    bait
            );
        }
    }

    @Override
    public ItemStack removeItem(
            int slot,
            int amount
    ) {

        ItemStack bait =
                getItem(slot);

        if (bait.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result =
                bait.copy();

        result.setCount(
                Math.min(
                        amount,
                        bait.getCount()
                )
        );

        bait.shrink(
                result.getCount()
        );

        setItem(
                slot,
                bait
        );

        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(
            int slot
    ) {

        ItemStack bait =
                getItem(slot);

        RodBaitData.removeBait(
                getRod()
        );

        return bait;
    }

    @Override
    public boolean isEmpty() {

        return getItem(0).isEmpty();
    }


    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(
            Player player
    ) {

        return true;
    }

    @Override
    public void clearContent() {

        RodBaitData.removeBait(
                getRod()
        );
    }

    @Override
    public void startOpen(
            Player player
    ) {
    }

    @Override
    public void stopOpen(
            Player player
    ) {
    }

    @Override
    public boolean canPlaceItem(
            int slot,
            ItemStack stack
    ) {

        return stack.getItem()
                instanceof Bait;
    }
}