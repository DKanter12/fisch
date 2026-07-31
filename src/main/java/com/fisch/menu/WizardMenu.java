package com.fisch.menu;

import com.fisch.command.ModCommands;
import com.fisch.entity.FishermanWizardEntity;
import com.fisch.registry.ModMenuTypes;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WizardMenu extends AbstractContainerMenu {
    private final Container container;
    private final FishermanWizardEntity wizard;

    public WizardMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public WizardMenu(int syncId, Inventory playerInventory, FishermanWizardEntity wizard) {
        super(ModMenuTypes.WIZARD_MENU, syncId);
        this.container = new SimpleContainer(1);
        this.wizard = wizard;

        // ЕДИНСТВЕННЫЙ СЛОТ: ровно на координате x: 80, y: 18
        this.addSlot(new Slot(this.container, 0, 80, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ModCommands.FISH_PRICES.containsKey(stack.getItem());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Инвентарь игрока
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Хотбар игрока
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public Container getContainer() {
        return this.container;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.container);
        if (this.wizard != null) {
            this.wizard.setTradingPlayer(null);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.wizard != null) {
            return this.wizard.isAlive() && player.distanceToSqr(this.wizard) <= 64.0D;
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            newStack = stackInSlot.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(stackInSlot, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.get(0).hasItem() || !ModCommands.FISH_PRICES.containsKey(stackInSlot.getItem())) {
                    return ItemStack.EMPTY;
                }
                ItemStack singleItem = stackInSlot.copy();
                singleItem.setCount(1);
                this.slots.get(0).set(singleItem);
                stackInSlot.shrink(1);
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return newStack;
    }
}