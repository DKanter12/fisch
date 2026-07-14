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
    // ТЕПЕРЬ ИСПОЛЬЗУЕТСЯ FishMongerEntity вместо FishMerchantEntity
    public static final EntityType<FishMongerEntity> FISH_MONGER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(FischMod.MODID, "fish_merchant"),
            FabricEntityTypeBuilder.create(MobCategory.CREATURE, FishMongerEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .build()
    );

    public static void registerModEntities() {
        FischMod.LOGGER.info("Регистрируем сущности для " + FischMod.MODID);
    }
}