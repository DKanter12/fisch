package com.fisch.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Bait extends Item {
    private final float speedBonus;
    private final float luckBonus;
    private final String buffKey;
    private final String debuffKey;

    // Конструктор по числовым статам
    public Bait(Properties properties, float speedBonus, float luckBonus) {
        this(properties, speedBonus, luckBonus, null, null);
    }


    // Конструктор по ключам локализации
    public Bait(Properties properties, String buffKey, @Nullable String debuffKey) {
        this(properties, 0.0f, 0.0f, buffKey, debuffKey);
    }

     // Полный конструктор (статы + тексты)
    public Bait(Properties properties, float speedBonus, float luckBonus, String buffKey, @Nullable String debuffKey){
            super(properties);
            this.speedBonus = speedBonus;
            this.luckBonus = luckBonus;
            this.buffKey = buffKey;
            this.debuffKey = debuffKey;
        }

            public float getSpeedBonus () {
                return speedBonus;
            }


                public float getLuckBonus () {
                    return luckBonus;
                }

                @Override
                public void appendHoverText (ItemStack stack, @Nullable Level
                level, List < Component > tooltip, TooltipFlag flag){
                    // 1. Отображение процентных статов
                    if (speedBonus > 0) {
                        tooltip.add(Component.literal("+" + (int) (speedBonus * 100) + "% Скорость клева").withStyle(ChatFormatting.GREEN));
                    } else if (speedBonus < 0) {
                        tooltip.add(Component.literal("-" + (int) (Math.abs(speedBonus) * 100) + "% Скорость клева").withStyle(ChatFormatting.RED));
                    }

                    if (luckBonus > 0) {
                        tooltip.add(Component.literal("+" + (int) (luckBonus * 100) + "% Удача").withStyle(ChatFormatting.GREEN));
                    } else if (luckBonus < 0) {
                        tooltip.add(Component.literal("-" + (int) (Math.abs(luckBonus) * 100) + "% Удача").withStyle(ChatFormatting.RED));
                    }

                    // 2. Отображение текстов баффов/дебаффов из lang-файлов
                    if (buffKey != null && !buffKey.isEmpty()) {
                        tooltip.add(Component.translatable(buffKey).withStyle(ChatFormatting.GREEN));
                    }
                    if (debuffKey != null && !debuffKey.isEmpty()) {
                        tooltip.add(Component.translatable(debuffKey).withStyle(ChatFormatting.RED));
                    }

                    super.appendHoverText(stack, level, tooltip, flag);
                }
            }
