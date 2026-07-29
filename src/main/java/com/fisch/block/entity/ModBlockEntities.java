package com.fisch.block.entity;

import com.fisch.FischMod;
import com.fisch.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final BlockEntityType<EnchantmentAltarBlockEntity> ENCHANTMENT_ALTAR_ENTITY =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    new ResourceLocation(FischMod.MODID, "enchantment_altar"),
                    BlockEntityType.Builder.of(EnchantmentAltarBlockEntity::new, ModBlocks.ENCHANTMENT_ALTAR).build(null)
            );

    public static void register() {
        FischMod.LOGGER.info("Registering block entities");
    }
}
