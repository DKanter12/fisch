package com.fisch.menu;

import com.fisch.command.ModCommands;
import com.fisch.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FishMerchantMenu extends AbstractContainerMenu {
    private final Container container;
    private final Villager merchant;

    public FishMerchantMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(syncId, playerInventory, new SimpleContainer(27), null);
    }


    public FishMerchantMenu(int syncId, Inventory playerInventory, Container container, Villager merchant) {
        super(ModMenuTypes.FISH_MERCHANT_MENU, syncId);
        checkContainerSize(container, 27);
        this.container = container;
        this.merchant = merchant;
        container.startOpen(playerInventory.player);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(container, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int getTotalPrice() {
        int total = 0;
        for (int i = 0; i < 27; i++) {
            ItemStack stack = this.container.getItem(i);
            if (!stack.isEmpty() && ModCommands.FISH_PRICES.containsKey(stack.getItem())) {
                total += ModCommands.FISH_PRICES.get(stack.getItem()) * stack.getCount();
            }
        }
        return total;
    }

    public Container getMerchantInventory() {
        return this.container;
    }

    @Override
    public boolean stillValid(Player player) {
        // Динамическая проверка расстояния и жизни ванильного скупщика
        boolean isCloseEnough = this.merchant == null || (this.merchant.isAlive() && this.merchant.distanceToSqr(player) <= 64.0D);
        return this.container.stillValid(player) && isCloseEnough;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < 27) {
                if (!this.moveItemStackTo(itemStack2, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, 27, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
        // Отпускаем жителя
        if (this.merchant != null && !player.level().isClientSide) {
            this.merchant.setTradingPlayer(null);
        }
    }
}