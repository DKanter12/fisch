package com.fisch.item;

import com.fisch.FischMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab; // Импортируем оригинальный класс игры
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs { // Переименовали класс, чтобы избежать конфликта

    public static final CreativeModeTab RODS_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(FischMod.MODID, "rods_tab"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.fisch.rods_tab"))
                    .icon(() -> new ItemStack(ModItems.ICE_ROD))
                    .displayItems((displayParameters, output) -> {
                        // Удочки
                        output.accept(ModItems.ICE_ROD);
                        output.accept(ModItems.SAND_ROD);
                        output.accept(ModItems.JUNGLE_ROD);

                        // Снасти и приманки
                        output.accept(ModItems.BUG_NET);
                        output.accept(ModItems.FISHING_BUG);
                        output.accept(ModItems.WORM);
                    })
                    .build()
    );

    // Этот метод обязательно нужно вызвать в FischMod, чтобы вкладка зарегистрировалась
    public static void register() {
        FischMod.LOGGER.info("Регистрация вкладок мода");
    }
}