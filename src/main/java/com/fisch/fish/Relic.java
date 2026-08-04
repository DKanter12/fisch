package com.fisch.fish;

import com.fisch.rod.RodEnchantment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.RandomSource;

import java.util.List;

public class Relic extends NewFish {

    public Relic(Properties properties, String name, int rarity, long minWeight, long maxWeight,
                 String bestBait, String bestWeather, String bestTime, String biomeGroup) {
        super(properties, name, rarity, minWeight, maxWeight, bestBait, bestWeather, bestTime, biomeGroup);
    }

    public static String getRelicEnchantment(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(RodEnchantment.NBT_KEY)) {
            return stack.getTag().getString(RodEnchantment.NBT_KEY);
        }
        return null;
    }

    public static void setRelicEnchantment(ItemStack stack, String ench) {
        stack.getOrCreateTag().putString(RodEnchantment.NBT_KEY, ench);
    }

    public static boolean hasEnchantment(ItemStack stack) {
        String ench = getRelicEnchantment(stack);
        return ench != null && !ench.isEmpty();
    }

    public static void setRandomEnchantment(ItemStack stack, RandomSource random) {
        if (random.nextFloat() < 0.5f) {
            setRelicEnchantment(stack, RodEnchantment.getWeightedRandomEnchantment(random));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String ench = getRelicEnchantment(stack);
        if (ench != null && !ench.isEmpty()) {
            // Берем переведенное имя зачарования
            tooltip.add(Component.translatable("enchantment.fisch." + ench.toLowerCase()).withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.translatable("message.fisch.relic.empty").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}