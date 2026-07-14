package com.fisch.menu;

import com.fisch.entity.FishMongerEntity;
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
        Entity entity = playerInventory.player.level().getEntity(buf.readInt());
        this.monger = entity instanceof FishMongerEntity ? (FishMongerEntity) entity : null;
    }

    // Конструктор для СЕРВЕРА
    public FishMongerMenu(int containerId, Inventory playerInventory, Item rodItem, FishMongerEntity monger) {
        super(ModMenuTypes.FISH_MONGER_MENU, containerId);
        this.rodItem = rodItem;
        this.monger = monger;
    }

    public boolean buyRod(ServerPlayer player) {
        long price = 1000;
        CurrencyHolder holder = (CurrencyHolder) player;

        if (holder.getMoney() >= price) {
            holder.setMoney(holder.getMoney() - price);
            player.getInventory().add(new ItemStack(this.rodItem));
            player.sendSystemMessage(Component.literal("§a[Продавец] Вы успешно купили удочку!"));
            return true;
        } else {
            player.sendSystemMessage(Component.literal("§c[Продавец] Недостаточно средств для покупки!"));
            return false;
        }
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