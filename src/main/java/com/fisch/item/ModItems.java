package com.fisch.item;

import com.fisch.fish.NewFish;
import com.fisch.rod.NewFishingRod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final NewFishingRod ICE_ROD = (NewFishingRod) registerItem("ice_rod", new NewFishingRod(new Item.Properties().stacksTo(1), 15f, 0.01f, 0.10f));
    public static final NewFishingRod SAND_ROD = (NewFishingRod) registerItem("sand_rod", new NewFishingRod(new Item.Properties().stacksTo(1), 25f, 0.05f, 0.10f));
    public static final NewFishingRod JUNGLE_ROD = (NewFishingRod) registerItem("jungle_rod", new NewFishingRod(new Item.Properties().stacksTo(1), 35f, 0.05f, 0.3f));

    public static final NewFish TEST_FISH = (NewFish) registerItem("test_fish", new NewFish(new Item.Properties(),"test fish", 8, 1,1,"dwd", "dwwd", "Ddw"));
    public static final NewFish[] ALL_FISH = {TEST_FISH};

    public static final Item BUG_NET = registerItem("bug_net",
            new BugNetItem(new Item.Properties().stacksTo(1))
    );

    // 2. Базовый жук (стакается до 64)
    public static final Item FISHING_BUG = registerItem("fishing_bug",
            new FishingBugItem(new Item.Properties())
    );

    // 3. Приманка-червь (самая слабая, параметры по 1.0f)
    public static final Item WORM = registerItem("worm_bait",
            new Bait(new Item.Properties(), 0.01f, 0.01f)
    );

    // Универсальный метод регистрации, который заменяет длинные ванильные вызовы
    private static Item registerItem(String name, Item item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                new ResourceLocation("fisch", name),
                item
        );
    }

    public static void register() {}
}