package com.fisch.mixin;

import com.fisch.item.Bait;
import com.fisch.rod.NewFishingRod;
import com.fisch.rod.RodBaitData;
import com.fisch.screen.BaitScreenHandler;
import com.fisch.screen.RodBaitContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
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

    @Inject(
            method = "use",
            at = @At("HEAD"),
            cancellable = true
    )
    private void requireBait(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Работает только для нашей кастомной удочки NewRod
        if (!(heldItem.getItem() instanceof NewFishingRod) && !(heldItem.getItem() instanceof FishingRodItem)) {
            return;
        }

        /*
         * Если меню приманки уже открыто — полностью блокируем стандартное использование,
         * чтобы клики по экрану или закрытие шифта не вызывали заброс удочки.
         */
        if (player.containerMenu instanceof BaitScreenHandler) {
            cir.setReturnValue(InteractionResultHolder.pass(heldItem));
            return;
        }

        /*
         * SHIFT + ПКМ — открываем меню приманки
         */
        if (player.isCrouching()) {

            if (!level.isClientSide &&
                    player instanceof ServerPlayer serverPlayer) {

                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (
                                        containerId,
                                        inventory,
                                        playerEntity
                                ) ->
                                        new BaitScreenHandler(
                                                containerId,
                                                inventory
                                        ),

                                Component.translatable(
                                        "screen.fisch.bait_menu"
                                )
                        )
                );
            }

            cir.setReturnValue(
                    InteractionResultHolder.success(
                            player.getItemInHand(hand)
                    )
            );

            return;
        }

        if (player.fishing == null) {
            ItemStack rod = player.getItemInHand(hand);

            ItemStack bait = RodBaitData.getBait(rod);

            if (bait.isEmpty()) {

                if (level.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("message.fisch.need_bait")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }

                cir.setReturnValue(InteractionResultHolder.fail(heldItem));
            }
        }
    }
}