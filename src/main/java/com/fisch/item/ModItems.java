package com.fisch.item;

import com.fisch.fish.NewFish;
import com.fisch.rod.NewRod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class ModItems {
    public static final NewRod TEST_FISHING_ROD = (NewRod) registerItem("test_rod", new NewRod(new Item.Properties(), 10f, 0.3f, 1f));

    public static final Item BUG_NET = registerItem("bug_net", new BugNetItem(new Item.Properties().durability(64)));
    public static final Item FISHING_BUG = registerItem("fishing_bug", new FishingBugItem(new Item.Properties()));
    public static final Item WORM = registerItem("worm_bait", new Bait(new Item.Properties(), 0.01f, 0.01f));

    private static final List<NewFish> FISH_LIST = new ArrayList<>();

    // ==========================================
    // ПУСТЫННЫЕ РЫБЫ (10 штук) - "desert"
    // ==========================================
    public static final NewFish SAND_GLIDER = registerFish("sand_glider", 8, 100, 500, "worm_bait", "clear", "day", "desert");
    public static final NewFish DUNE_GUPPY = registerFish("dune_guppy", 8, 50, 200, "none", "clear", "day", "desert");
    public static final NewFish MIRAGE_FIN = registerFish("mirage_fin", 7, 150, 600, "none", "clear", "sunset", "desert");
    public static final NewFish CACTUS_SPIKE = registerFish("cactus_spike", 7, 200, 800, "fishing_bug", "clear", "day", "desert");
    public static final NewFish OASIS_SHIMMER = registerFish("oasis_shimmer", 6, 300, 1200, "fishing_bug", "clear", "night", "desert");
    public static final NewFish DUST_DEVIL_EEL = registerFish("dust_devil_eel", 5, 500, 2500, "worm_bait", "clear", "day", "desert");
    public static final NewFish SCARAOH = registerFish("scaraoh", 5, 400, 1800, "fishing_bug", "clear", "night", "desert");
    public static final NewFish PHARAOH_BASS = registerFish("pharaoh_bass", 4, 1000, 5000, "fishing_bug", "clear", "day", "desert");
    public static final NewFish SUN_SERPENT = registerFish("sun_serpent", 3, 2000, 10000, "none", "clear", "day", "desert");
    public static final NewFish GLASS_DUNE_PIERCER = registerFish("glass_dune_piercer", 2, 800, 4000, "none", "clear", "sunrise", "desert");

    // ==========================================
    // ЛЕДЯНЫЕ РЫБЫ (14 штук) - "ice"
    // ==========================================
    public static final NewFish FROST_MINNOW = registerFish("frost_minnow", 8, 30, 150, "worm_bait", "raining", "day", "ice");
    public static final NewFish SHIVER_TAIL = registerFish("shiver_tail", 8, 40, 200, "none", "clear", "day", "ice");
    public static final NewFish GLACIER_PERCH = registerFish("glacier_perch", 8, 150, 700, "none", "clear", "day", "ice");
    public static final NewFish AURORA_TETRA = registerFish("aurora_tetra", 7, 60, 300, "fishing_bug", "clear", "night", "ice");
    public static final NewFish ICICLE_PIKE = registerFish("icicle_pike", 7, 500, 2200, "worm_bait", "raining", "day", "ice");
    public static final NewFish SNOWBALL_PUFFER = registerFish("snowball_puffer", 7, 100, 600, "none", "clear", "day", "ice");
    public static final NewFish DEEP_FROST_COD = registerFish("deep_frost_cod", 6, 400, 1800, "fishing_bug", "clear", "night", "ice");
    public static final NewFish BLIZZARD_VORTEX = registerFish("blizzard_vortex", 6, 200, 1000, "none", "raining", "night", "ice");
    public static final NewFish CRYO_CRAB = registerFish("cryo_crab", 5, 300, 1500, "worm_bait", "clear", "day", "ice");
    public static final NewFish FROSTBITE_SNAPPER = registerFish("frostbite_snapper", 5, 600, 3000, "fishing_bug", "raining", "day", "ice");
    public static final NewFish PERMAFROST_GOLIATH = registerFish("permafrost_goliath", 4, 5000, 25000, "fishing_bug", "raining", "day", "ice");
    public static final NewFish ICE_SCULPTURE_KOI = registerFish("ice_sculpture_koi", 4, 1200, 6000, "none", "clear", "sunrise", "ice");
    public static final NewFish WINTER_SPIRIT = registerFish("winter_spirit", 3, 800, 4000, "none", "clear", "night", "ice");
    public static final NewFish ABSOLUTE_ZERO = registerFish("absolute_zero", 2, 1000, 5000, "none", "clear", "night", "ice");

    // ==========================================
    // ДЖУНГЛЕВЫЕ РЫБЫ (16 штук) - "jungle"
    // ==========================================
    public static final NewFish BAMBOO_BORER = registerFish("bamboo_borer", 8, 80, 400, "worm_bait", "clear", "day", "jungle");
    public static final NewFish MUD_CRAWLER = registerFish("mud_crawler", 8, 120, 600, "none", "raining", "day", "jungle");
    public static final NewFish CANOPY_FLYER = registerFish("canopy_flyer", 7, 150, 750, "fishing_bug", "clear", "day", "jungle");
    public static final NewFish ORCHID_GUPPY = registerFish("orchid_guppy", 6, 40, 200, "none", "clear", "day", "jungle");
    public static final NewFish VINE_CONSTRICTOR_EEL = registerFish("vine_constrictor_eel", 6, 300, 1500, "worm_bait", "clear", "night", "jungle");
    public static final NewFish PIRANHA_KING = registerFish("piranha_king", 5, 200, 1000, "fishing_bug", "raining", "day", "jungle");
    public static final NewFish JAGUAR_CATFISH = registerFish("jaguar_catfish", 5, 800, 4000, "worm_bait", "clear", "night", "jungle");
    public static final NewFish VENOM_FANG = registerFish("venom_fang", 5, 250, 1200, "fishing_bug", "clear", "night", "jungle");
    public static final NewFish ACID_DISCUS = registerFish("acid_discus", 5, 180, 900, "none", "raining", "day", "jungle");
    public static final NewFish SWAMP_LURKER = registerFish("swamp_lurker", 5, 450, 2000, "none", "raining", "night", "jungle");
    public static final NewFish ANCIENT_TOTEM_FISH = registerFish("ancient_totem_fish", 4, 1500, 7000, "fishing_bug", "clear", "day", "jungle");
    public static final NewFish JUNGLE_HEART_CARP = registerFish("jungle_heart_carp", 4, 2000, 9000, "none", "clear", "sunset", "jungle");
    public static final NewFish TEMPLE_GUARDIAN_EEL = registerFish("temple_guardian_eel", 4, 3500, 15000, "fishing_bug", "clear", "day", "jungle");
    public static final NewFish FEATHERED_SERPENT = registerFish("feathered_serpent", 3, 4000, 18000, "none", "raining", "day", "jungle");
    public static final NewFish SPIRIT_OF_THE_CANOPY = registerFish("spirit_of_the_canopy", 3, 1000, 5000, "none", "clear", "night", "jungle");
    public static final NewFish PREDATOR_PRIME = registerFish("predator_prime", 2, 5000, 22000, "fishing_bug", "clear", "night", "jungle");

    // ==========================================
    // РЫБЫ ОБЫЧНЫХ БИОМОВ (16 штук) - "plain"
    // ==========================================
    public static final NewFish RIVER_MINNOW = registerFish("river_minnow", 8, 20, 100, "worm_bait", "clear", "day", "plain");
    public static final NewFish POND_LOACH = registerFish("pond_loach", 8, 30, 150, "none", "raining", "day", "plain");
    public static final NewFish GRASSY_CARP = registerFish("grassy_carp", 8, 200, 1000, "none", "clear", "day", "plain");
    public static final NewFish MUD_GUDGEON = registerFish("mud_gudgeon", 8, 50, 250, "none", "clear", "day", "plain");
    public static final NewFish OAK_LEAF_TAIL = registerFish("oak_leaf_tail", 8, 80, 400, "worm_bait", "clear", "day", "plain");
    public static final NewFish BIRCH_BAR_DACE = registerFish("birch_bark_dace", 8, 90, 450, "worm_bait", "clear", "day", "plain");
    public static final NewFish CLOVER_FISH = registerFish("clover_fish", 7, 40, 200, "fishing_bug", "clear", "day", "plain");
    public static final NewFish APPLE_CHEEK_GUPPY = registerFish("apple_cheek_guppy", 7, 30, 150, "none", "clear", "day", "plain");
    public static final NewFish DANDELION_FLOAT = registerFish("dandelion_float", 7, 50, 250, "none", "clear", "day", "plain");
    public static final NewFish CHERRY_BLOSSOM_KOI = registerFish("cherry_blossom_koi", 6, 600, 3000, "none", "clear", "sunrise", "plain");
    public static final NewFish MOSS_BACK_TROUT = registerFish("moss_back_trout", 6, 400, 2000, "worm_bait", "raining", "day", "plain");
    public static final NewFish RAINBOW_TROUT = registerFish("rainbow_trout", 6, 300, 1500, "fishing_bug", "clear", "day", "plain");
    public static final NewFish AMBER_EYE_PERCH = registerFish("amber_eye_perch", 6, 350, 1800, "fishing_bug", "clear", "night", "plain");
    public static final NewFish FOREST_SPIRIT_MONARCH = registerFish("forest_spirit_monarch", 4, 3000, 12000, "fishing_bug", "clear", "day", "plain");
    public static final NewFish WHISPERING_WILLOW_EEL = registerFish("whispering_willow_eel", 3, 2500, 10000, "none", "clear", "night", "plain");
    public static final NewFish NATURE_INCARNATE = registerFish("nature_incarnate", 2, 4000, 16000, "none", "clear", "day", "plain");

    // ==========================================
    // МУСОР (3 штуки) - "junk" (Редкость 10)
    // ==========================================
    public static final NewFish SEAWEED = registerFish("seaweed", 10, 50, 200, "none", "clear", "day", "junk");
    public static final NewFish IRON_SCRAP = registerFish("iron_scrap", 10, 100, 1000, "none", "clear", "day", "junk");
    public static final NewFish OLD_BOOT = registerFish("old_boot", 10, 500, 1500, "none", "clear", "day", "junk");

    public static final NewFish[] ALL_FISH;

    static {
        // Убрали добавление TEST_FISH
        ALL_FISH = FISH_LIST.toArray(new NewFish[0]);
    }

    private static NewFish registerFish(String id, int rarity, long minWeight, long maxWeight, String bestBait, String bestWeather, String bestTime, String biomeGroup) {
        NewFish fish = new NewFish(new Item.Properties(), id, rarity, minWeight, maxWeight, bestBait, bestWeather, bestTime, biomeGroup);
        FISH_LIST.add(fish);
        return (NewFish) registerItem(id, fish);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                new ResourceLocation("fisch", name),
                item
        );
    }

    public static void register() {
    }
}