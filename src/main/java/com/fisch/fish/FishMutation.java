package com.fisch.fish;

import com.fisch.command.ModCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Random;

public enum FishMutation {
    NONE("None", 1.0F, ChatFormatting.WHITE),

    // Новые мутации
    SHINY("Shiny", 2.0F, ChatFormatting.YELLOW),
    SPARKLING("Sparkling", 2.2F, ChatFormatting.AQUA),

    // Остальные мутации
    ALBINO("Albino", 1.5F, ChatFormatting.WHITE),
    DARKENED("Darkened", 1.6F, ChatFormatting.DARK_GRAY),
    MOSAIC("Mosaic", 1.8F, ChatFormatting.AQUA),
    NEGATIVE("Negative", 1.7F, ChatFormatting.DARK_PURPLE),
    FROZEN("Frozen", 1.9F, ChatFormatting.BLUE),
    SCORCHED("Scorched", 2.0F, ChatFormatting.RED),
    AURORA("Aurora", 2.5F, ChatFormatting.LIGHT_PURPLE),
    MYTHICAL("Mythical", 4.0F, ChatFormatting.GOLD),
    ELECTRIC("Electric", 2.2F, ChatFormatting.YELLOW),
    GREEDY("Greedy", 3.0F, ChatFormatting.GREEN),
    HEXED("Hexed", 2.8F, ChatFormatting.DARK_GREEN),
    ABYSSAL("Abyssal", 3.5F, ChatFormatting.DARK_BLUE),
    CHAOTIC("Chaotic", 3.2F, ChatFormatting.RED),
    GHASTLY("Ghastly", 2.7F, ChatFormatting.GRAY),
    ANOMALOUS("Anomalous", 4.5F, ChatFormatting.DARK_AQUA),
    QUANTUM("Quantum", 5.0F, ChatFormatting.LIGHT_PURPLE),
    MOONKISSED("Moonkissed", 3.3F, ChatFormatting.WHITE),
    SANGUINE("Sanguine", 3.6F, ChatFormatting.DARK_RED),
    SINISTER("Sinister", 3.8F, ChatFormatting.DARK_GRAY),
    SOLARBLAZE("Solarblaze", 4.2F, ChatFormatting.GOLD),
    CELESTIAL("Celestial", 6.0F, ChatFormatting.AQUA),
    ATLANTEAN("Atlantean", 4.8F, ChatFormatting.BLUE),
    LUNAR("Lunar", 3.4F, ChatFormatting.GRAY),
    SUNDRIED("Sundried", 1.4F, ChatFormatting.YELLOW),
    CLOVER("Clover", 2.5F, ChatFormatting.GREEN),
    DIVINE("Divine", 7.0F, ChatFormatting.GOLD),
    AETHER("Aether", 5.5F, ChatFormatting.WHITE),
    FURY("Fury", 3.9F, ChatFormatting.RED),
    VOIDTOUCHED("Voidtouched", 6.5F, ChatFormatting.DARK_PURPLE),
    TRYHARD("Tryhard", 2.1F, ChatFormatting.DARK_RED),
    SHADY("Shady", 1.8F, ChatFormatting.DARK_GRAY);

    private final String name;
    private final float priceMultiplier;
    private final ChatFormatting color;
    private static final Random RANDOM = new Random();

    FishMutation(String name, float priceMultiplier, ChatFormatting color) {
        this.name = name;
        this.priceMultiplier = priceMultiplier;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return name;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public float getPriceMultiplier() {
        return priceMultiplier;
    }

    public static boolean hasMutation(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("FishMutation");
    }

    public static FishMutation getMutation(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("FishMutation")) {
            try {
                return valueOf(tag.getString("FishMutation"));
            } catch (IllegalArgumentException e) {
                return NONE;
            }
        }
        return NONE;
    }

    public static FishMutation getRandom() {
        if (RANDOM.nextFloat() < 0.2f) {
            return NONE;
        }

        FishMutation[] mutations = values();
        int index = 1 + RANDOM.nextInt(mutations.length - 1);
        return mutations[index];
    }

    public static long getEnchantCost(ItemStack stack) {
        Item item = stack.getItem();
        int basePrice = ModCommands.FISH_PRICES.getOrDefault(item, 100);
        return Math.round(basePrice * 1.5f);
    }

    public static void applyMutation(ItemStack stack, FishMutation mutation) {
        CompoundTag tag = stack.getOrCreateTag();
        if (mutation != NONE) {
            tag.putString("FishMutation", mutation.name());
        }
    }

    public static int calculatePrice(int basePrice, ItemStack stack) {
        FishMutation mutation = getMutation(stack);
        return Math.round(basePrice * mutation.getPriceMultiplier());
    }
}