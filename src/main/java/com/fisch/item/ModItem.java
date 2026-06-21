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

    public static final NewRod WOODEN_ROD = (NewRod) registerItem("wooden_rod", 
            new NewRod(new Item.Properties().durability(100), 15f, 0.1f, 1.0f));

    public static final NewRod LUCKY_ROD = (NewRod) registerItem("lucky_rod", 
            new NewRod(new Item.Properties().durability(150), 12f, 0.4f, 0.8f));

    public static final NewRod PLASTIC_ROD = (NewRod) registerItem("plastic_rod", 
            new NewRod(new Item.Properties().durability(80), 20f, -0.1f, 1.4f));

    public static final NewRod TRAINING_ROD = (NewRod) registerItem("training_rod", 
            new NewRod(new Item.Properties().durability(120), 10f, 0.0f, 1.1f));

    public static final NewRod CARBON_ROD = (NewRod) registerItem("carbon_rod", 
            new NewRod(new Item.Properties().durability(250), 25f, 0.2f, 1.2f));

    private static Item registerItem(String name, Item item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.tryParse(MODID + ":" + name),
                item
        );
    }

    public static void register() {}
}