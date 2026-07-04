package com.fisch.item;

import com.fisch.rod.NewRod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import static com.fisch.FischMod.MODID;

public class ModItem {
    private static final Item TEST = registerItem("test", new Item(new Item.Properties()));
    private static final NewRod TEST_FISHING_ROD = (NewRod) registerItem("test_rod", new NewRod(new Item.Properties(), 10f, 0.3f, 1f));


    private static Item registerItem(String name, Item item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.tryParse(MODID + ":" + name),
                item
        );
    }

    public static void register() {}
}