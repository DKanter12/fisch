package com.fisch.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Bait extends Item {

    private final float resilience;
    private final float lucky;

    // Используем net.minecraft.world.item.Item.Properties вместо Settings
    public Bait(Properties properties, float resilience, float lucky) {
        super(properties);
        this.resilience = resilience;
        this.lucky = lucky;
    }

    public float getResilience() {
        return this.resilience;
    }

    public float getLucky() {
        return this.lucky;
    }


}