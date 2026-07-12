package com.fisch.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.fisch.util.CurrencyHolder;
import com.fisch.networking.ModPackets;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class ModCommands {

    // КАТАЛОГ ЦЕН: Запоминает предмет (Item) и его цену (Integer)
    public static final Map<Item, Integer> FISH_PRICES = new HashMap<>();

    // ДОБАВЛЕНО: Базовые цены при запуске сервера
    static {
        FISH_PRICES.put(Items.COD, 10);          // Сырая треска
        FISH_PRICES.put(Items.SALMON, 20);       // Сырой лосось
        FISH_PRICES.put(Items.TROPICAL_FISH, 40);// Тропическая рыба
        FISH_PRICES.put(Items.PUFFERFISH, 50);   // Иглобрюх (рыба-ёж)
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // Команда /addcoins
            dispatcher.register(Commands.literal("addcoins")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                CurrencyHolder holder = (CurrencyHolder) player;
                                holder.setMoney(holder.getMoney() + amount);
                                ModPackets.syncMoney(player);
                                context.getSource().sendSystemMessage(Component.literal("§a[Fisch] Выдано " + amount + " C$!"));
                                return 1;
                            })));

            // Команда /fishprice <цена>
            dispatcher.register(Commands.literal("fishprice")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("price", IntegerArgumentType.integer(0))
                            .executes(context -> {
                                int price = IntegerArgumentType.getInteger(context, "price");
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                ItemStack handItem = player.getMainHandItem();

                                if (handItem.isEmpty()) {
                                    context.getSource().sendFailure(Component.literal("§cВозьми рыбу в руку, чтобы установить на неё цену!"));
                                    return 0;
                                }

                                // Записываем новую цену в базу
                                FISH_PRICES.put(handItem.getItem(), price);

                                context.getSource().sendSystemMessage(Component.literal("§e[Fisch] Цена за " + handItem.getHoverName().getString() + " установлена: " + price + " C$"));

                                return 1;
                            })));
        });
    }
}