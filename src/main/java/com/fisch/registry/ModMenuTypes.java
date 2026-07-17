package com.fisch.registry;

import com.fisch.FischMod;
import com.fisch.menu.FishMerchantMenu;
import com.fisch.menu.FishMongerMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {

    // Используем ExtendedScreenHandlerType, он требует (syncId, inventory, buf)
    public static final MenuType<FishMerchantMenu> FISH_MERCHANT_MENU =
            Registry.register(
                    BuiltInRegistries.MENU,
                    new ResourceLocation(FischMod.MODID, "fish_merchant_menu"),
                    new ExtendedScreenHandlerType<>(FishMerchantMenu::new)
            );

    public static final MenuType<FishMongerMenu> FISH_MONGER_MENU =
            Registry.register(
                    BuiltInRegistries.MENU,
                    new ResourceLocation(FischMod.MODID, "fish_monger_menu"),
                    new ExtendedScreenHandlerType<>(FishMongerMenu::new)
            );

    public static void registerMenus() {}
}