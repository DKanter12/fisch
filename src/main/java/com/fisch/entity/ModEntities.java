package com.fisch.entity;

import com.fisch.FischMod;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    // Рыбак-Чародей (Fisherman Wizard)
    public static final EntityType<FishermanWizardEntity> FISHERMAN_WIZARD = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(FischMod.MODID, "fisherman_wizard"),
            FabricEntityTypeBuilder.create(MobCategory.CREATURE, FishermanWizardEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .build()
    );

    // Продавец удочек (Fish Monger)
    public static final EntityType<FishMongerEntity> FISH_MONGER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(FischMod.MODID, "fish_merchant"),
            FabricEntityTypeBuilder.create(MobCategory.CREATURE, FishMongerEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .build()
    );

    // Адский Продавец удочек (Fish Monger Nether)
    public static final EntityType<FishMongerNetherEntity> FISH_MONGER_NETHER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(FischMod.MODID, "fish_monger_nether"),
            FabricEntityTypeBuilder.create(MobCategory.CREATURE, FishMongerNetherEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .build()
    );

    public static void registerModEntities() {
        FischMod.LOGGER.info("Регистрируем сущности для " + FischMod.MODID);
    }
}