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

    public static final CreativeModeTab FISCH_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(FischMod.MODID, "fisch_tab"),
            FabricItemGroup.builder()
                    // Название вкладки из файла локализации
                    .title(Component.translatable("itemGroup.fisch.fisch_tab"))
                    // Иконка вкладки
                    .icon(() -> new ItemStack(ModItems.RAINBOW_TROUT))
                    .displayItems((displayParameters, output) -> {

                        // Пустынные рыбы
                        output.accept(ModItems.SAND_GLIDER);
                        output.accept(ModItems.DUNE_GUPPY);
                        output.accept(ModItems.MIRAGE_FIN);
                        output.accept(ModItems.CACTUS_SPIKE);
                        output.accept(ModItems.OASIS_SHIMMER);
                        output.accept(ModItems.DUST_DEVIL_EEL);
                        output.accept(ModItems.SCARAOH);
                        output.accept(ModItems.PHARAOH_BASS);
                        output.accept(ModItems.SUN_SERPENT);
                        output.accept(ModItems.GLASS_DUNE_PIERCER);

                        // Ледяные рыбы
                        output.accept(ModItems.FROST_MINNOW);
                        output.accept(ModItems.SHIVER_TAIL);
                        output.accept(ModItems.GLACIER_PERCH);
                        output.accept(ModItems.AURORA_TETRA);
                        output.accept(ModItems.ICICLE_PIKE);
                        output.accept(ModItems.SNOWBALL_PUFFER);
                        output.accept(ModItems.DEEP_FROST_COD);
                        output.accept(ModItems.BLIZZARD_VORTEX);
                        output.accept(ModItems.CRYO_CRAB);
                        output.accept(ModItems.FROSTBITE_SNAPPER);
                        output.accept(ModItems.PERMAFROST_GOLIATH);
                        output.accept(ModItems.ICE_SCULPTURE_KOI);
                        output.accept(ModItems.WINTER_SPIRIT);
                        output.accept(ModItems.ABSOLUTE_ZERO);

                        // Джунглевые рыбы
                        output.accept(ModItems.BAMBOO_BORER);
                        output.accept(ModItems.MUD_CRAWLER);
                        output.accept(ModItems.CANOPY_FLYER);
                        output.accept(ModItems.ORCHID_GUPPY);
                        output.accept(ModItems.VINE_CONSTRICTOR_EEL);
                        output.accept(ModItems.PIRANHA_KING);
                        output.accept(ModItems.JAGUAR_CATFISH);
                        output.accept(ModItems.VENOM_FANG);
                        output.accept(ModItems.ACID_DISCUS);
                        output.accept(ModItems.SWAMP_LURKER);
                        output.accept(ModItems.ANCIENT_TOTEM_FISH);
                        output.accept(ModItems.JUNGLE_HEART_CARP);
                        output.accept(ModItems.TEMPLE_GUARDIAN_EEL);
                        output.accept(ModItems.FEATHERED_SERPENT);
                        output.accept(ModItems.SPIRIT_OF_THE_CANOPY);
                        output.accept(ModItems.PREDATOR_PRIME);

                        // Обычные рыбы
                        output.accept(ModItems.RIVER_MINNOW);
                        output.accept(ModItems.POND_LOACH);
                        output.accept(ModItems.GRASSY_CARP);
                        output.accept(ModItems.MUD_GUDGEON);
                        output.accept(ModItems.OAK_LEAF_TAIL);
                        output.accept(ModItems.BIRCH_BAR_DACE);
                        output.accept(ModItems.CLOVER_FISH);
                        output.accept(ModItems.APPLE_CHEEK_GUPPY);
                        output.accept(ModItems.DANDELION_FLOAT);
                        output.accept(ModItems.CHERRY_BLOSSOM_KOI);
                        output.accept(ModItems.MOSS_BACK_TROUT);
                        output.accept(ModItems.RAINBOW_TROUT);
                        output.accept(ModItems.AMBER_EYE_PERCH);
                        output.accept(ModItems.FOREST_SPIRIT_MONARCH);
                        output.accept(ModItems.WHISPERING_WILLOW_EEL);
                        output.accept(ModItems.NATURE_INCARNATE);

                        // Мусор
                        output.accept(ModItems.SEAWEED);
                        output.accept(ModItems.IRON_SCRAP);
                        output.accept(ModItems.OLD_BOOT);
                    })
                    .build()
    );

    // Этот метод обязательно нужно вызвать в FischMod, чтобы вкладка зарегистрировалась
    public static void register() {
        FischMod.LOGGER.info("Регистрация вкладок мода");
    }
}