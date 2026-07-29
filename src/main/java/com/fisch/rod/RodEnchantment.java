package com.fisch.rod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class RodEnchantment {
    public static final String LUCKY = "lucky";
    public static final String DIVINE = "divine";
    public static final String STEADY = "steady";
    public static final String CONTROLLED = "controlled";
    public static final String RESILIENT = "resilient";
    public static final String MYSTICAL = "mystical";

    public static final String[] ALL = {LUCKY, DIVINE, STEADY, CONTROLLED, RESILIENT, MYSTICAL};
    public static final String[] LUCK = {LUCKY, DIVINE};
    public static final String[] CONTROL = {STEADY, CONTROLLED};
    public static final String[] RESILIENCE = {RESILIENT, MYSTICAL};

    public static final String NBT_KEY = "fisch_enchantment";

    public static String getRandomEnchantment(Random random) {
        return ALL[random.nextInt(ALL.length)];
    }

    public static String getWeightedRandomEnchantment(Random random) {
        return getWeightedRandom(random.nextFloat());
    }

    public static String getWeightedRandomEnchantment(RandomSource random) {
        return getWeightedRandom(random.nextFloat());
    }

    private static String getWeightedRandom(float roll) {
        if (roll < 0.25f) return LUCKY;
        if (roll < 0.45f) return CONTROLLED;
        if (roll < 0.63f) return RESILIENT;
        if (roll < 0.78f) return STEADY;
        if (roll < 0.90f) return MYSTICAL;
        return DIVINE;
    }

    public static float getLuckBonus(String ench) {
        if (ench == null) return 0;
        return switch (ench) {
            case LUCKY -> 1.0f;
            case DIVINE -> 3.0f;
            default -> 0f;
        };
    }

    public static float getControlBonus(String ench) {
        if (ench == null) return 0;
        return switch (ench) {
            case STEADY -> 0.02f;
            case CONTROLLED -> 0.05f;
            default -> 0f;
        };
    }

    public static float getResilienceBonus(String ench) {
        if (ench == null) return 0;
        return switch (ench) {
            case RESILIENT -> 0.02f;
            case MYSTICAL -> 0.05f;
            default -> 0f;
        };
    }

    public static Component getDisplayName(String ench) {
        if (ench == null) return Component.literal("");
        Component name = switch (ench) {
            case LUCKY -> Component.translatable("enchantment.fisch.lucky");
            case DIVINE -> Component.translatable("enchantment.fisch.divine");
            case STEADY -> Component.translatable("enchantment.fisch.steady");
            case CONTROLLED -> Component.translatable("enchantment.fisch.controlled");
            case RESILIENT -> Component.translatable("enchantment.fisch.resilient");
            case MYSTICAL -> Component.translatable("enchantment.fisch.mystical");
            default -> Component.literal(ench);
        };
        return Component.translatable("enchantment.fisch.format", name).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    public static String getCategory(String ench) {
        if (ench == null) return "";
        for (String s : LUCK) if (s.equals(ench)) return "luck";
        for (String s : CONTROL) if (s.equals(ench)) return "control";
        for (String s : RESILIENCE) if (s.equals(ench)) return "resilience";
        return "";
    }

    public static String getEnchantment(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_KEY)) {
            return stack.getTag().getString(NBT_KEY);
        }
        return null;
    }

    public static void setEnchantment(ItemStack stack, String ench) {
        stack.getOrCreateTag().putString(NBT_KEY, ench);
    }
}
