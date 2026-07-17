package com.fisch.events;

import com.fisch.item.ModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class ModEvents {

    public static void register() {
        // ОСТАВЛЯЕМ ТОЛЬКО ЛОГИКУ ДЛЯ ЧЕРВЯКОВ (если они нужны)
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide()) {
                return;
            }
            // Выпадение червяка из земли/травы
            if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
                if (world.random.nextFloat() < 0.05F) {
                    player.addItem(new ItemStack(ModItems.WORM));
                }
            }
        });

        // ВТОРОЙ БЛОК, КОТОРЫЙ СПАВНИЛ ЖУКОВ ИЗ ВСЕГО — УДАЛЕН!
    }
}