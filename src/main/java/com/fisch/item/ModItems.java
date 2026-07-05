package com.fisch.item;

import com.fisch.fish.NewFish;
import com.fisch.rod.NewRod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import static com.fisch.FischMod.MODID;

public class ModItems {
    private static final NewRod TEST_FISHING_ROD = (NewRod) registerItem("test_rod", new NewRod(new Item.Properties(), 10f, 0.3f, 1f));
    public static final NewFish TEST_FISH = new NewFish("test", 8, 1,1,"dwd", "dwwd", "Ddw");
    public static final NewFish[] ALL_FISH = {TEST_FISH};

    private static Item registerItem(String name, Item item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.tryParse(MODID + ":" + name),
                item
        );
    }

    public static void register() {}
}