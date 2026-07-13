package com.fisch.mixin;

import com.fisch.item.Bait;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void requireBait(Level level, Player player, InteractionHand hand,
                             CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

        if (player.fishing == null) {
            ItemStack bait = player.getOffhandItem();

            if (!(bait.getItem() instanceof Bait)) {

                if (level.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("message.fisch.need_bait")
                                    .withStyle(ChatFormatting.RED),
                            true // отображается над хотбаром
                    );
                }

                cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
            }
        }
    }
}