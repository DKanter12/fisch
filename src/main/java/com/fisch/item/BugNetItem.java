package com.fisch.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    // === ТРАТИМ ПРОЧНОСТЬ ПРИ ЛОМАНИИ ЛЮБЫХ БЛОКОВ (включая траву и листья) ===
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide) {
            // Наносим 1 единицу урона сачку в руке
            stack.hurtAndBreak(1, miningEntity, (entity) -> {
                entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }
        return true;
    }

    // === ТРАТИМ ПРОЧНОСТЬ ПРИ УДАРЕ (если бьем мобов или жуков на карте) ===
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, (entity) -> {
            entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        return true;
    }
}