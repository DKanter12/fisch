package com.fisch.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BaitItem extends Item {
    private final String buffKey;
    private final String debuffKey;

    // Конструктор принимает ключи локализации для баффа и дебаффа
    public BaitItem(Properties properties, String buffKey, String debuffKey) {
        super(properties);
        this.buffKey = buffKey;
        this.debuffKey = debuffKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        // Добавляем бафф зеленым цветом, если он есть
        if (this.buffKey != null && !this.buffKey.isEmpty()) {
            tooltipComponents.add(Component.translatable(this.buffKey).withStyle(ChatFormatting.GREEN));
        }

        // Добавляем дебафф красным цветом, если он есть
        if (this.debuffKey != null && !this.debuffKey.isEmpty()) {
            tooltipComponents.add(Component.translatable(this.debuffKey).withStyle(ChatFormatting.RED));
        }

        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}