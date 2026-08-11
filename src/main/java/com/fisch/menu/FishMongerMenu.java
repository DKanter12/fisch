package com.fisch.menu;

import com.fisch.entity.FishMongerEntity;
import com.fisch.entity.FishMongerNetherEntity;
import com.fisch.item.ModItems;
import com.fisch.registry.ModMenuTypes;
import com.fisch.util.CurrencyHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FishMongerMenu extends AbstractContainerMenu {

    public final Item rodItem;
    private final Entity monger; // Изменено на Entity, чтобы подходило для всех жителей

    // Конструктор для КЛИЕНТА
    public FishMongerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(ModMenuTypes.FISH_MONGER_MENU, containerId);
        this.rodItem = BuiltInRegistries.ITEM.byId(buf.readInt());
        this.monger = null;
    }

    // Конструктор для СЕРВЕРА
    public FishMongerMenu(int containerId, Inventory playerInventory, Item rodItem, Entity monger) {
        super(ModMenuTypes.FISH_MONGER_MENU, containerId);
        this.rodItem = rodItem;
        this.monger = monger;
    }

    public long getPriceForItem(Item item) {
        if (item == ModItems.ICE_ROD) return 2500;
        if (item == ModItems.SAND_ROD) return 5000;
        if (item == ModItems.JUNGLE_ROD) return 10000;
        if (item == ModItems.SWAMP_ROD) return 15000;
        if (item == ModItems.MUSHROOM_ROD) return 25000;
        if (item == ModItems.HELL_ROD) return 50000; // Установи свою цену здесь!

        return 0;
    }

    public boolean buyRod(ServerPlayer player) {
        long price = getPriceForItem(this.rodItem);
        CurrencyHolder holder = (CurrencyHolder) player;

        if (holder.getMoney() < price) {
            long remaining = price - holder.getMoney();
            player.sendSystemMessage(Component.translatable("message.fisch.monger.not_enough_money", remaining));
            return false;
        }

        if (player.getInventory().getFreeSlot() == -1) {
            player.sendSystemMessage(Component.translatable("message.fisch.monger.inventory_full"));
            return false;
        }

        holder.setMoney(holder.getMoney() - price);
        player.getInventory().add(new ItemStack(this.rodItem));

        Component itemName = this.rodItem.getName(new ItemStack(this.rodItem));
        player.sendSystemMessage(Component.translatable("message.fisch.monger.buy_success", itemName, price));
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.monger == null || (this.monger.isAlive() && this.monger.distanceToSqr(player) <= 64.0D);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.monger != null && !player.level().isClientSide) {
            // Сбрасываем торговлю в зависимости от того, какой это моб
            if (this.monger instanceof FishMongerEntity normalMonger) {
                normalMonger.setTradingPlayer(null);
            } else if (this.monger instanceof FishMongerNetherEntity netherMonger) {
                netherMonger.setTradingPlayer(null);
            }
        }
    }
}