package com.fisch.block;

import com.fisch.FischMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public class ModBlocks {
    public static final Block ENCHANTMENT_ALTAR = registerBlock("enchantment_altar",
            new EnchantmentAltarBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .pushReaction(PushReaction.BLOCK)
            )
    );

    private static Block registerBlock(String name, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(FischMod.MODID, name), block);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(FischMod.MODID, name),
                new BlockItem(block, new Item.Properties()));
        return block;
    }

    public static void register() {
        FischMod.LOGGER.info("Registering blocks");
    }
}
