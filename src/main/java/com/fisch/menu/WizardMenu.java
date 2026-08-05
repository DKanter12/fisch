package com.fisch.menu;

import com.fisch.command.ModCommands;
import com.fisch.entity.FishermanWizardEntity;
import com.fisch.fish.FishMutation;
import com.fisch.networking.ModPackets;
import com.fisch.registry.ModMenuTypes;
import com.fisch.util.CurrencyHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class WizardMenu extends AbstractContainerMenu {
    private final Container container;
    private final FishermanWizardEntity wizard;
    private static final Random RANDOM = new Random();

    public WizardMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public WizardMenu(int syncId, Inventory playerInventory, FishermanWizardEntity wizard) {
        super(ModMenuTypes.WIZARD_MENU, syncId);
        this.container = new SimpleContainer(1);
        this.wizard = wizard;

        // Слот для рыбы
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

    public void enchantFish(ServerPlayer player) {
        ItemStack fish = this.container.getItem(0);

        // 1. Проверяем, лежит ли валидная рыба
        if (fish.isEmpty() || !ModCommands.FISH_PRICES.containsKey(fish.getItem())) {
            return;
        }

        long cost = FishMutation.getEnchantCost(fish);
        CurrencyHolder currencyHolder = (CurrencyHolder) player;

        // 2. Проверяем баланс
        if (currencyHolder.getMoney() < cost) {
            player.sendSystemMessage(Component.translatable("message.fisch.wizard.not_enough_money"));
            return;
        }

        // 3. Снимаем деньги
        currencyHolder.setMoney(currencyHolder.getMoney() - cost);
        ModPackets.syncMoney(player);

        // 4. ШАНС 1 К 7 НА ПРОВАЛ
        boolean isFail = (RANDOM.nextInt(7) == 0);

        if (isFail) {
            // ПРОВАЛ: Просто сбрасываем мутацию/чары, но рыбу можно чаровать дальше
            FishMutation.clearMutation(fish);
            player.sendSystemMessage(Component.translatable("message.fisch.wizard.fail"));
        } else {
            // УСПЕХ: Накладываем случайную мутацию
            FishMutation newMutation = FishMutation.getRandom();
            FishMutation.applyMutation(fish, newMutation);
            player.sendSystemMessage(Component.translatable(
                    "message.fisch.wizard.success",
                    Component.literal(newMutation.getDisplayName()).withStyle(newMutation.getColor())
            ));
        }

        // Обновляем слот в меню
        this.container.setChanged();
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