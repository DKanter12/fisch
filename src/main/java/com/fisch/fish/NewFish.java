package com.fisch.fish;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NewFish extends Item {
    public String name;
    public int rarity;
    long minWeight;
    long maxWeight;
    public String bestBait;
    public String bestWeather;
    public String bestTime;
    private final String biomeGroup;

    public NewFish(Properties properties, String name, int rarity, long minWeight, long maxWeight, String bestBait, String bestWeather, String bestTime, String biomeGroup) {
        super(properties);
        this.name = name;
        this.rarity = rarity;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.bestBait = bestBait;
        this.bestWeather = bestWeather;
        this.bestTime = bestTime;
        this.biomeGroup = biomeGroup;
    }

    public String getBiomeGroup() {
        return this.biomeGroup;
    }

    // Красим название рыбы в инвентаре в зависимости от редкости!
    @Override
    public Component getName(ItemStack stack) {
        ChatFormatting color = switch (this.rarity) {
            case 10 -> ChatFormatting.DARK_GRAY;  // Мусор
            case 8 -> ChatFormatting.WHITE;       // Common
            case 7 -> ChatFormatting.GREEN;       // Uncommon
            case 6 -> ChatFormatting.DARK_AQUA;   // Unusual
            case 5 -> ChatFormatting.LIGHT_PURPLE;// Rare
            case 4 -> ChatFormatting.YELLOW;      // лег
            case 3 -> ChatFormatting.RED;         // миф
            case 2, 1 -> ChatFormatting.DARK_RED;     // Exotic)
            default -> ChatFormatting.WHITE;
        };

        if (this.rarity <= 2) {
            return Component.translatable(this.getDescriptionId(stack)).withStyle(color).withStyle(ChatFormatting.BOLD);
        }
        return Component.translatable(this.getDescriptionId(stack)).withStyle(color);
    }
}