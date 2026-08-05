package com.fisch.networking;

import com.fisch.FischMod;
import com.fisch.command.ModCommands;
import com.fisch.fish.FishMutation;
import com.fisch.menu.FishMerchantMenu;
import com.fisch.menu.WizardMenu;
import com.fisch.util.CurrencyHolder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ModPackets {
    public static final ResourceLocation ENCHANT_FISH_C2S = new ResourceLocation(FischMod.MODID, "enchant_fish");
    public static final ResourceLocation SYNC_MONEY_S2C = new ResourceLocation("fisch", "money_sync");
    public static final ResourceLocation SELL_ITEMS_C2S = new ResourceLocation("fisch", "sell_items");
    public static final ResourceLocation BUY_ROD_C2S = new ResourceLocation("fisch", "buy_rod");
    public static final ResourceLocation OPEN_BAIT_MENU = new ResourceLocation(FischMod.MODID, "open_bait_menu");

    public static void syncMoney(ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeLong(((CurrencyHolder) player).getMoney());
        ServerPlayNetworking.send(player, SYNC_MONEY_S2C, buf);
    }

    public static void register() {
        // Перенаправляем логику зачарования в сам WizardMenu, где реализован шанс 1 к 7 и порча рыбы
        ServerPlayNetworking.registerGlobalReceiver(ENCHANT_FISH_C2S, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player.containerMenu instanceof WizardMenu wizardMenu) {
                    wizardMenu.enchantFish(player);
                }
            });
        });

        // Продажа рыбы
        ServerPlayNetworking.registerGlobalReceiver(SELL_ITEMS_C2S, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player.containerMenu instanceof FishMerchantMenu menu) {
                    long totalValue = 0;
                    Container merchantInventory = menu.getMerchantInventory();
                    for (int i = 0; i < 27; i++) {
                        ItemStack stack = merchantInventory.getItem(i);
                        if (!stack.isEmpty() && ModCommands.FISH_PRICES.containsKey(stack.getItem())) {
                            int basePrice = ModCommands.FISH_PRICES.get(stack.getItem());
                            int finalPrice = FishMutation.calculatePrice(basePrice, stack);

                            totalValue += (long) stack.getCount() * finalPrice;
                            merchantInventory.setItem(i, ItemStack.EMPTY);
                        }
                    }
                    if (totalValue > 0) {
                        CurrencyHolder holder = (CurrencyHolder) player;
                        holder.setMoney(holder.getMoney() + totalValue);
                        syncMoney(player);
                        player.sendSystemMessage(Component.translatable("text.fisch.sell_success", totalValue));
                    }
                }
            });
        });

        // Покупка удочки
        ServerPlayNetworking.registerGlobalReceiver(BUY_ROD_C2S, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player.containerMenu instanceof com.fisch.menu.FishMongerMenu menu) {
                    if (menu.buyRod(player)) {
                        syncMoney(player);
                    }
                }
            });
        });
    }
}