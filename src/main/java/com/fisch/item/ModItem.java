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

    public static final NewRod WOODEN_ROD = (NewRod) registerItem("training_rod", 
            new NewRod(new Item.Properties(), -70f, 0.2f, 20f));

    public static final NewRod LUCKY_ROD = (NewRod) registerItem("plastic_rod", 
            new NewRod(new Item.Properties(), 15f, 0.01f, 10f));

    public static final NewRod PLASTIC_ROD = (NewRod) registerItem("fungal_rod", 
            new NewRod(new Item.Properties(), 45, 0.01f, 0f));

    public static final NewRod TRAINING_ROD = (NewRod) registerItem("training_rod", 
            new NewRod(new Item.Properties(), 10f, 0.0f, 1.1f));

    public static final NewRod CARBON_ROD = (NewRod) registerItem("carbon_rod", 
            new NewRod(new Item.Properties(), 25f, 0.05f, 10f));

    private static Item registerItem(String name, Item item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.tryParse(MODID + ":" + name),
                item
        );
    }

    public static void register() {}
}