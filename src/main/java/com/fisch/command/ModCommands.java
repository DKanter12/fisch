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
        FISH_PRICES.put(Items.COD, 10);
        FISH_PRICES.put(Items.SALMON, 20);
        FISH_PRICES.put(Items.TROPICAL_FISH, 40);
        FISH_PRICES.put(Items.PUFFERFISH, 50);

        Random random = new Random();

        for (NewFish fish : ModItems.ALL_FISH) {
            random.setSeed(fish.name.hashCode());

            int basePrice = 20;
            int variance = 5;

            switch (fish.rarity) {
                case 10 -> { basePrice = 1; variance = 0; }
                case 8 -> { basePrice = 15; variance = 10; }
                case 7 -> { basePrice = 35; variance = 15; }
                case 6 -> { basePrice = 75; variance = 30; }
                case 5 -> { basePrice = 150; variance = 60; }
                case 4 -> { basePrice = 450; variance = 150; }
                case 3 -> { basePrice = 1200; variance = 400; }
                case 2, 1 -> { basePrice = 3500; variance = 1000;}
            }

            int finalPrice = basePrice;
            if (variance > 0) {
                finalPrice += random.nextInt(variance * 2 + 1) - variance;
            }

            FISH_PRICES.put(fish, finalPrice);
        }
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

                                // ИСПОЛЬЗУЕМ КЛЮЧ ПЕРЕВОДА
                                context.getSource().sendSystemMessage(Component.translatable("command.fisch.addcoins.success", amount));
                                return 1;
                            })));

            // Команда /fishprice
            dispatcher.register(Commands.literal("fishprice")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("price", IntegerArgumentType.integer(0))
                            .executes(context -> {
                                int price = IntegerArgumentType.getInteger(context, "price");
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                ItemStack handItem = player.getMainHandItem();

                                if (handItem.isEmpty()) {
                                    // ИСПОЛЬЗУЕМ КЛЮЧ ПЕРЕВОДА ДЛЯ ОШИБКИ
                                    context.getSource().sendFailure(Component.translatable("command.fisch.fishprice.empty_hand"));
                                    return 0;
                                }

                                FISH_PRICES.put(handItem.getItem(), price);
                                // ИСПОЛЬЗУЕМ КЛЮЧ ПЕРЕВОДА С ДВУМЯ ПЕРЕМЕННЫМИ (название предмета и цена)
                                context.getSource().sendSystemMessage(Component.translatable("command.fisch.fishprice.success", handItem.getHoverName().getString(), price));
                                return 1;
                            })));
        });
    }
}