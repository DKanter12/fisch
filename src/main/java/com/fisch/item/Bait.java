package com.fisch.item;

import net.minecraft.world.item.Item;

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