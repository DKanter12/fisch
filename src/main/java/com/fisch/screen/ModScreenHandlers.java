package com.fisch.screen;

import com.fisch.FischMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {

    public static final MenuType<BaitScreenHandler> BAIT_MENU =
            Registry.register(
                    BuiltInRegistries.MENU,

                    new ResourceLocation(
                            FischMod.MODID,
                            "bait_menu"
                    ),

                    new MenuType<>(
                            BaitScreenHandler::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static void register() {

        FischMod.LOGGER.info(
                "Registering screen handlers"
        );
    }
}