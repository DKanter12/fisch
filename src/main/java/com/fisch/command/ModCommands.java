package com.fisch.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.fisch.util.CurrencyHolder;
import com.fisch.networking.ModPackets;
import com.fisch.fish.NewFish;
import com.fisch.item.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ModCommands {

    public static final Map<Item, Integer> FISH_PRICES = new HashMap<>();

    static {
        // Базовые ванильные цены
        FISH_PRICES.put(Items.COD, 10);
        FISH_PRICES.put(Items.SALMON, 20);
        FISH_PRICES.put(Items.TROPICAL_FISH, 40);
        FISH_PRICES.put(Items.PUFFERFISH, 50);

        Random random = new Random();

        // МАГИЯ ЗДЕСЬ: Расчет случайных цен с наслоением
        for (NewFish fish : ModItems.ALL_FISH) {

            // Мы передаем имя рыбы как seed.
            // Это значит, что для конкретной рыбы рандом выдаст ВСЕГДА одно и то же число.
            // Например, "river_minnow" всегда будет стоить 18, а "pond_loach" - 12.
            random.setSeed(fish.name.hashCode());

            int basePrice = 20;
            int variance = 5; // Насколько цена может отклоняться (+ или -)

            switch (fish.rarity) {
                case 10 -> { basePrice = 1; variance = 0; }      // Мусор: Всегда 1
                case 8 -> { basePrice = 15; variance = 10; }     // Common: от 5 до 25
                case 7 -> { basePrice = 35; variance = 15; }     // Uncommon: от 20 до 50
                case 6 -> { basePrice = 75; variance = 30; }     // Unusual: от 45 до 105 (может быть 100!)
                case 5 -> { basePrice = 150; variance = 60; }    // Rare: от 90 до 210 (тоже может быть 100!)
                case 4 -> { basePrice = 450; variance = 150; }   // Legendary: от 300 до 600
                case 3 -> { basePrice = 1200; variance = 400; }  // Mythical: от 800 до 1600
                case 2, 1 -> { basePrice = 3500; variance = 1000;}// Exotic: от 2500 до 4500
            }

            int finalPrice = basePrice;
            if (variance > 0) {
                // Прибавляем или вычитаем случайное число из разброса
                finalPrice += random.nextInt(variance * 2 + 1) - variance;
            }

            FISH_PRICES.put(fish, finalPrice);
        }
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // Команда /addcoins <кол-во>
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

                                FISH_PRICES.put(handItem.getItem(), price);
                                context.getSource().sendSystemMessage(Component.literal("§e[Fisch] Цена за " + handItem.getHoverName().getString() + " установлена: " + price + " C$"));
                                return 1;
                            })));
        });
    }
}