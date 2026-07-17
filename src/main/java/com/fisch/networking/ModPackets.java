package com.fisch.networking;

import com.fisch.FischMod;
import com.fisch.command.ModCommands;
import com.fisch.menu.FishMerchantMenu;
import com.fisch.screen.BaitScreenHandler;
import com.fisch.util.CurrencyHolder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;

public class ModPackets {
    public static final ResourceLocation SYNC_MONEY_S2C = new ResourceLocation(FischMod.MODID, "money_sync");
    public static final ResourceLocation SELL_ITEMS_C2S = new ResourceLocation(FischMod.MODID, "sell_items");
    public static final ResourceLocation OPEN_BAIT_MENU = new ResourceLocation(FischMod.MODID, "open_bait_menu");

    public static void syncMoney(ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeLong(((CurrencyHolder) player).getMoney());
        ServerPlayNetworking.send(player, SYNC_MONEY_S2C, buf);
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SELL_ITEMS_C2S, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player.containerMenu instanceof FishMerchantMenu menu) {
                    long totalValue = 0;
                    Container merchantInventory = menu.getMerchantInventory();

                    // ИСПРАВЛЕНО: Считаем цену динамически из ModCommands.FISH_PRICES
                    for (int i = 0; i < 27; i++) {
                        ItemStack stack = merchantInventory.getItem(i);

                        // Если слот не пустой и предмет есть в нашем каталоге цен
                        if (!stack.isEmpty() && ModCommands.FISH_PRICES.containsKey(stack.getItem())) {
                            int price = ModCommands.FISH_PRICES.get(stack.getItem());
                            totalValue += (long) stack.getCount() * price;

                            // Удаляем проданную рыбу
                            merchantInventory.setItem(i, ItemStack.EMPTY);
                        }
                    }

                    if (totalValue > 0) {
                        CurrencyHolder holder = (CurrencyHolder) player;
                        holder.setMoney(holder.getMoney() + totalValue);
                        syncMoney(player);
                        player.sendSystemMessage(Component.literal("§a[Рыботорговец] Успешно продано на сумму " + totalValue + " C$!"));
                    }
                }
            });
        });

            ServerPlayNetworking.registerGlobalReceiver(
                    OPEN_BAIT_MENU,

                    (server, player, handler, buf, responseSender) -> {

                        server.execute(() -> {

                            if (!(player.getMainHandItem().getItem()
                                    instanceof FishingRodItem)) {
                                return;
                            }

                            player.openMenu(
                                    new SimpleMenuProvider(

                                            (containerId, inventory, playerEntity) ->
                                                    new BaitScreenHandler(
                                                            containerId,
                                                            inventory
                                                    ),

                                            Component.translatable(
                                                    "screen.fisch.bait_menu"
                                            )
                                    )
                            );
                        });
                    }
            );
    }
}