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

public class ModCommands {

    public static final Map<Item, Integer> FISH_PRICES = new HashMap<>();

    static {
        // Базовые ванильные цены
        FISH_PRICES.put(Items.COD, 10);
        FISH_PRICES.put(Items.SALMON, 20);
        FISH_PRICES.put(Items.TROPICAL_FISH, 40);
        FISH_PRICES.put(Items.PUFFERFISH, 50);

        // МАГИЯ ЗДЕСЬ: Автоматически рассчитываем и задаем цену для ВСЕХ наших 56 кастомных рыб на основе их редкости!
        for (NewFish fish : ModItems.ALL_FISH) {
            int autoPrice = switch (fish.rarity) {
                case 10 -> 1;    // Мусор (Junk) -> всего 1 монета
                case 8 -> 15;    // Common (Обычная) -> 15 монет
                case 7 -> 35;    // Uncommon (Необычная) -> 35 монет
                case 6 -> 75;    // Unusual (Редкая) -> 75 монет
                case 5 -> 150;   // Rare (Очень Редкая) -> 150 монет
                case 4 -> 450;   // Legendary (Легендарная) -> 450 монет
                case 3 -> 1200;  // Mythical (Мифическая) -> 1200 монет
                case 2 -> 3500;  // Exotic (Экзотическая) -> 3500 монет
                default -> 20;
            };
            FISH_PRICES.put(fish, autoPrice);
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