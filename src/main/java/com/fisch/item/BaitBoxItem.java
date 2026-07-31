package com.fisch.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BaitBoxItem extends Item {

    public BaitBoxItem(Properties properties) {
        // Устанавливаем размер стака ровно 1
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            RandomSource random = level.getRandom();

            Item[] availableBaits = new Item[] {
                    ModItems.WORM,
                    ModItems.FISHING_BUG,
                    ModItems.BLACK_FISH_EGGS,
                    ModItems.BAIT_BLEND,
                    ModItems.CRAB_CLAW,
                    ModItems.SEA_CUCUMBER
            };

            int dropRolls = 3 + random.nextInt(4);

            for (int i = 0; i < dropRolls; i++) {
                Item randomBait = availableBaits[random.nextInt(availableBaits.length)];
                int count = 1 + random.nextInt(3);
                ItemStack loot = new ItemStack(randomBait, count);

                if (!player.getInventory().add(loot)) {
                    player.drop(loot, false);
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);

            // Тратим ящик, если игрок не в Творческом режиме
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        // consume вместо sidedSuccess заставит игру сразу же "съесть" предмет
        return InteractionResultHolder.consume(itemStack);
    }
}