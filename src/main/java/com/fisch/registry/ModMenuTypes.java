package com.fisch.registry;

import com.fisch.menu.FishMerchantMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {
    public static final MenuType<FishMerchantMenu> FISH_MERCHANT_MENU = Registry.register(
            BuiltInRegistries.MENU,
            new ResourceLocation("fisch", "fish_merchant_menu"),
            new MenuType<>(FishMerchantMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );
}