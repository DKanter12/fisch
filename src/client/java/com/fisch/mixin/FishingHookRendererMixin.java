package com.fisch.mixin;

import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingHookRenderer.class)
public class FishingHookRendererMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"
            )
    )
    private boolean allowCustomRods(ItemStack stack, Item item) {
        if (item == Items.FISHING_ROD) {
            return stack.getItem() instanceof FishingRodItem;
        }

        return stack.is(item);
    }
}
