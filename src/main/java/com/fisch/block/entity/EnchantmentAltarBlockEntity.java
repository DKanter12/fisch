package com.fisch.block.entity;

import com.fisch.screen.EnchantmentAltarScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantmentAltarBlockEntity extends BlockEntity implements Container, MenuProvider {
    private NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    public EnchantmentAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTMENT_ALTAR_ENTITY, pos, state);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < this.inventory.size() ? this.inventory.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.inventory.size()) {
            this.inventory.set(slot, stack);
            if (stack.getCount() > this.getMaxStackSize()) {
                stack.setCount(this.getMaxStackSize());
            }
            this.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
        this.setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.fisch.enchantment_altar");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new EnchantmentAltarScreenHandler(syncId, inv, this);
    }
}
