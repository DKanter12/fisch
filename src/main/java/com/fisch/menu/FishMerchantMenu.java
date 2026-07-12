package com.fisch.menu;

import com.fisch.command.ModCommands;
import com.fisch.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FishMerchantMenu extends AbstractContainerMenu {
    private final Container merchantInventory;
    private final Villager merchant;

    // Слот для синхронизации суммы между сервером и экраном (клиентом)
    private final DataSlot currentTotalPrice = DataSlot.standalone();

    public FishMerchantMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(27), null);
    }

    public FishMerchantMenu(int containerId, Inventory playerInventory, Container merchantInventory, Villager merchant) {
        super(ModMenuTypes.FISH_MERCHANT_MENU, containerId);
        this.merchantInventory = merchantInventory;
        this.merchant = merchant;

        // Регистрируем DataSlot, чтобы он сам отправлял данные на экран
        this.addDataSlot(this.currentTotalPrice);

        merchantInventory.startOpen(playerInventory.player);

        // СЛУШАТЕЛЬ: Каждый раз, когда в инвентарь кладут или забирают предмет, мы вызываем пересчет
        if (merchantInventory instanceof SimpleContainer simpleContainer) {
            simpleContainer.addListener(container -> this.slotsChanged(container));
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(merchantInventory, j + i * 9, 8 + j * 18, 18 + i * 18));
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

    // Этот метод срабатывает автоматом при ЛЮБОМ движении предметов в верхних слотах
    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.merchantInventory) {
            int total = 0;
            for (int i = 0; i < this.merchantInventory.getContainerSize(); i++) {
                ItemStack stack = this.merchantInventory.getItem(i);
                // Если предмет есть в нашем каталоге FISH_PRICES
                if (!stack.isEmpty() && ModCommands.FISH_PRICES.containsKey(stack.getItem())) {
                    total += stack.getCount() * ModCommands.FISH_PRICES.get(stack.getItem());
                }
            }
            this.currentTotalPrice.set(total); // Обновляем сумму!
        }
    }

    // Метод, чтобы экран (Screen) мог узнать текущую сумму
    public int getTotalPrice() {
        return this.currentTotalPrice.get();
    }

    public Container getMerchantInventory() {
        return this.merchantInventory;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.merchantInventory.stopOpen(player);

        // Надежное возвращение предметов прямо в инвентарь:
        for (int i = 0; i < this.merchantInventory.getContainerSize(); i++) {
            ItemStack stack = this.merchantInventory.getItem(i);
            if (!stack.isEmpty()) {
                // Пытаемся положить предмет обратно в инвентарь игрока
                if (!player.getInventory().add(stack)) {
                    // И ТОЛЬКО если инвентарь забит полностью — выкидываем перед игроком, чтобы рыба не удалилась
                    player.drop(stack, false);
                }
                // Очищаем слот торговца
                this.merchantInventory.setItem(i, ItemStack.EMPTY);
            }
        }

        // Отпускаем торговца по его делам
        if (this.merchant != null) {
            this.merchant.setTradingPlayer(null);
        }
    }
}