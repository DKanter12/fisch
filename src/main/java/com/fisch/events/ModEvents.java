package com.fisch.events;

import com.fisch.item.ModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ModEvents {

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide()) {
                return;
            }
            if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {

                if (world.random.nextFloat() < 0.05F) {

                    player.addItem(new ItemStack(ModItems.WORM));

                }
            }
        });
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {

            if (world.isClientSide()) {
                return;
            }

            // Ломаем только высокую траву

            // Проверяем предмет в руке
            ItemStack tool = player.getMainHandItem();

            if (!tool.is(ModItems.BUG_NET)) {
                return;
            }

            if (world.random.nextFloat() < 0.10) {

                Block.popResource(
                        world,
                        pos,
                        new ItemStack(ModItems.FISHING_BUG)
                );
            }
        });
    }
}
