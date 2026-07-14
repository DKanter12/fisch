package com.fisch.menu;

import com.fisch.entity.FishMongerEntity;
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
    private final FishMongerEntity monger; // Ссылка на жителя

    // Конструктор для КЛИЕНТА
    public FishMongerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(ModMenuTypes.FISH_MONGER_MENU, containerId);
        this.rodItem = BuiltInRegistries.ITEM.byId(buf.readInt());
        // На клиенте сущность может быть недоступна, используем null, если не нужно
        this.monger = null;
    }
    public FishMongerMenu(int containerId, Inventory playerInventory, Item rodItem, FishMongerEntity monger) {
        super(ModMenuTypes.FISH_MONGER_MENU, containerId);
        this.rodItem = rodItem;
        this.monger = monger;
    }

    // Добавь этот вспомогательный метод в класс FishMongerMenu
    // Найди в FishMongerMenu метод getPriceForItem и поменяй private на public
    public long getPriceForItem(Item item) {
        if (item == ModItems.ICE_ROD) return 2500;
        if (item == ModItems.SAND_ROD) return 5000;
        if (item == ModItems.JUNGLE_ROD) return 10000;
        return 99999;
    }

    public boolean buyRod(ServerPlayer player) {
        long price = getPriceForItem(this.rodItem);
        CurrencyHolder holder = (CurrencyHolder) player;

        // 1. Сначала проверяем деньги
        if (holder.getMoney() < price) {
            long remaining = price - holder.getMoney();
            player.sendSystemMessage(Component.literal("§c[Продавец] Недостаточно средств! Нужно ещё " + remaining + " C$."));
            return false;
        }

        // 2. НОВАЯ ПРОВЕРКА: Если в инвентаре нет ни одного свободного слота
        if (player.getInventory().getFreeSlot() == -1) {
            player.sendSystemMessage(Component.literal("§c[Продавец] Твой инвентарь переполнен! Освободи место."));
            return false;
        }

        // 3. Если всё ок — снимаем деньги и выдаем предмет
        holder.setMoney(holder.getMoney() - price);
        player.getInventory().add(new ItemStack(this.rodItem));
        player.sendSystemMessage(Component.literal("§a[Продавец] Вы купили " + this.rodItem.getName(new ItemStack(this.rodItem)).getString() + " за " + price + " C$!"));
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        // Если житель умер или игрок отошел слишком далеко — меню закроется
        return this.monger == null || (this.monger.isAlive() && this.monger.distanceToSqr(player) <= 64.0D);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Когда меню закрывается, житель перестает смотреть на игрока и может бежать
        if (this.monger != null && !player.level().isClientSide) {
            this.monger.setTradingPlayer(null);
        }
    }
}