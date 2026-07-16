package com.fisch.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BugNetItem extends Item {

    public BugNetItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // Если блок каменистый (требует кирку) — скорость ломания 0
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return 0.0F;
        }
        return super.getDestroySpeed(stack, state);
    }

    // === ТРАТИМ ПРОЧНОСТЬ И СПАВНИМ ЖУКА С ШАНСОМ 25% ===
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide) {
            // Наносим 1 единицу урона сачку в руке
            stack.hurtAndBreak(1, miningEntity, (entity) -> {
                entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });

            // Проверяем, сломал ли игрок нужную растительность
            if (isBugSourceBlock(state)) {
                // Шанс 25% (0.25)
                if (level.random.nextFloat() < 0.15f) {
                    // Создаем сущность предмета жука прямо на координатах сломанного блока
                    ItemEntity bugEntity = new ItemEntity(
                            level,
                            pos.getX() + 0.5,
                            pos.getY() + 0.1,
                            pos.getZ() + 0.5,
                            new ItemStack(ModItems.FISHING_BUG)
                    );

                    // Небольшая задержка перед тем, как игрок сможет его автоматически подобрать
                    bugEntity.setDefaultPickUpDelay();

                    // Добавляем сущность в мир
                    level.addFreshEntity(bugEntity);
                }
            }
        }
        return true;
    }

    /**
     * Проверяет, подходит ли блок для выпадения жука.
     * Разрешены только: обычная трава, высокая трава, тростник и лианы.
     */
    private boolean isBugSourceBlock(BlockState state) {
        Block block = state.getBlock();
        return     block == Blocks.GRASS       // Обычная трава (в старых версиях)
                || block == Blocks.TALL_GRASS  // Высокая трава
                || block == Blocks.SUGAR_CANE  // Тростник
                || block == Blocks.VINE;       // Лианы
    }

    // === ТРАТИМ ПРОЧНОСТЬ ПРИ УДАРЕ ===
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, (entity) -> {
            entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        return true;
    }
}