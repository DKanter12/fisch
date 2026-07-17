package com.fisch.rod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RodBaitData {

    private static final String BAIT_TAG = "FischBait";
    private static final String ITEM_TAG = "Item";
    private static final String COUNT_TAG = "Count";

    public static ItemStack getBait(ItemStack rod) {

        CompoundTag tag = rod.getTag();

        if (tag == null || !tag.contains(BAIT_TAG)) {
            return ItemStack.EMPTY;
        }

        CompoundTag baitTag =
                tag.getCompound(BAIT_TAG);

        ResourceLocation itemId =
                ResourceLocation.tryParse(
                        baitTag.getString(ITEM_TAG)
                );

        if (itemId == null) {
            return ItemStack.EMPTY;
        }

        int count =
                baitTag.getInt(COUNT_TAG);

        if (count <= 0) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(
                BuiltInRegistries.ITEM.get(itemId),
                count
        );
    }

    public static void setBait(
            ItemStack rod,
            ItemStack bait
    ) {

        if (bait.isEmpty()) {
            removeBait(rod);
            return;
        }

        CompoundTag tag =
                rod.getOrCreateTag();

        CompoundTag baitTag =
                new CompoundTag();

        ResourceLocation itemId =
                BuiltInRegistries.ITEM.getKey(
                        bait.getItem()
                );

        baitTag.putString(
                ITEM_TAG,
                itemId.toString()
        );

        baitTag.putInt(
                COUNT_TAG,
                bait.getCount()
        );

        tag.put(
                BAIT_TAG,
                baitTag
        );
    }

    public static void removeBait(
            ItemStack rod
    ) {

        CompoundTag tag =
                rod.getTag();

        if (tag != null) {
            tag.remove(BAIT_TAG);
        }
    }

    public static void consumeBait(
            ItemStack rod,
            int amount
    ) {

        ItemStack bait =
                getBait(rod);

        if (bait.isEmpty()) {
            return;
        }

        bait.shrink(amount);

        if (bait.isEmpty()) {
            removeBait(rod);
        } else {
            setBait(rod, bait);
        }
    }
}