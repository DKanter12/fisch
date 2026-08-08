package com.fisch.screen;

import com.fisch.block.entity.EnchantmentAltarBlockEntity;
import com.fisch.fish.Relic;
import com.fisch.rod.RodEnchantment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class EnchantmentAltarScreenHandler extends AbstractContainerMenu {
    private final net.minecraft.world.Container fishContainer;

    public EnchantmentAltarScreenHandler(int containerId, Inventory inventory) {
        this(containerId, inventory, new net.minecraft.world.SimpleContainer(2));
    }

    public EnchantmentAltarScreenHandler(int containerId, Inventory inventory, net.minecraft.world.Container fishContainer) {
        super(ModScreenHandlers.ENCHANTMENT_ALTAR_MENU, containerId);
        this.fishContainer = fishContainer;

        fishContainer.startOpen(inventory.player);

        this.addSlot(new RodSlot(fishContainer, 0, 44, 35));
        this.addSlot(new RelicSlot(fishContainer, 1, 116, 35));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.fishContainer.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.fishContainer.stopOpen(player);
    }

    public boolean enchantRod(Player player) {
        ItemStack rodStack = fishContainer.getItem(0);
        ItemStack relicStack = fishContainer.getItem(1);

        if (rodStack.isEmpty() || !(rodStack.getItem() instanceof FishingRodItem)) {
            player.displayClientMessage(Component.translatable("message.fisch.enchant.no_rod").withStyle(ChatFormatting.RED), true);
            if (player instanceof ServerPlayer sp) sp.closeContainer();
            return false;
        }

        if (relicStack.isEmpty() || !(relicStack.getItem() instanceof Relic)) {
            player.displayClientMessage(Component.translatable("message.fisch.enchant.no_relic").withStyle(ChatFormatting.RED), true);
            if (player instanceof ServerPlayer sp) sp.closeContainer();
            return false;
        }

        if (player.level().isClientSide) {
            return false;
        }

        long timeOfDay = player.level().getDayTime() % 24000;
        if (timeOfDay < 13000 || timeOfDay >= 23000) {
            player.displayClientMessage(Component.translatable("message.fisch.enchant.night_only").withStyle(ChatFormatting.RED), true);
            if (player instanceof ServerPlayer sp) sp.closeContainer();
            return false;
        }

        if (player.totalExperience < 400) {
            player.displayClientMessage(Component.translatable("message.fisch.enchant.no_xp").withStyle(ChatFormatting.RED), true);
            if (player instanceof ServerPlayer sp) sp.closeContainer();
            return false;
        }

        if (!relicStack.isEmpty() && relicStack.getItem() instanceof Relic) {
            String enchantment = Relic.hasEnchantment(relicStack)
                    ? Relic.getRelicEnchantment(relicStack)
                    : RodEnchantment.getWeightedRandomEnchantment(new Random());
            player.giveExperiencePoints(-400);
            relicStack.shrink(1);
            RodEnchantment.setEnchantment(rodStack, enchantment);

            if (player instanceof ServerPlayer sp) {
                sp.displayClientMessage(
                        Component.translatable("message.fisch.enchant.success")
                                .append(RodEnchantment.getDisplayName(enchantment)),
                        true
                );

                BlockPos pos = sp.blockPosition();
                if (this.fishContainer instanceof EnchantmentAltarBlockEntity altar) {
                    pos = altar.getBlockPos();
                }
                ServerLevel level = sp.serverLevel();
                level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 50, 0.5, 0.5, 0.5, 0.5);
                level.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 20, 0.5, 0.5, 0.5, 0.1);
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.5F, 1.5F);

                sp.closeContainer();
            }
            return true;
        }

        return false;

    }
}
