package com.fisch.rod;

import com.fisch.FischMod;
import com.fisch.item.Bait;
import com.fisch.screen.BaitScreenHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class NewFishingRod
        extends FishingRodItem {


    private final float luck;

    private final float control;

    private final float resilience;


    public NewFishingRod(
            Properties properties,
            float luck,
            float control,
            float resilience
    ) {

        super(properties);

        this.luck =
                luck;

        this.control =
                control;

        this.resilience =
                resilience;
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        ItemStack itemStack =
                player.getItemInHand(hand);


        /*
         * =====================================================
         * SHIFT + ПКМ
         *
         * ОТКРЫВАЕМ МЕНЮ ПРИМАНКИ
         * =====================================================
         */

        if (
                player.isCrouching()
                        &&
                        player.fishing == null
        ) {

            if (
                    !level.isClientSide
                            &&
                            player instanceof ServerPlayer serverPlayer
            ) {

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


            return InteractionResultHolder.sidedSuccess(
                    itemStack,
                    level.isClientSide()
            );
        }


        /*
         * =====================================================
         * ЕСЛИ ПОПЛАВОК УЖЕ ЗАБРОШЕН
         *
         * ЗАБИРАЕМ ЕГО
         * =====================================================
         */

        if (
                player.fishing != null
        ) {

            int damage =
                    0;


            if (
                    !level.isClientSide
            ) {

                damage =
                        player.fishing.retrieve(
                                itemStack
                        );


                itemStack.hurtAndBreak(
                        damage,
                        player,

                        p ->
                                p.broadcastBreakEvent(
                                        hand
                                )
                );
            }


            level.playSound(
                    null,

                    player.getX(),
                    player.getY(),
                    player.getZ(),

                    SoundEvents.FISHING_BOBBER_RETRIEVE,

                    SoundSource.NEUTRAL,

                    1.0F,

                    0.4F /
                            (
                                    level.getRandom()
                                            .nextFloat()
                                            * 0.4F
                                            + 0.8F
                            )
            );


            player.gameEvent(
                    GameEvent.ITEM_INTERACT_FINISH
            );


            return InteractionResultHolder.sidedSuccess(
                    itemStack,
                    level.isClientSide()
            );
        }


        /*
         * =====================================================
         * ПРОВЕРКА ПРИМАНКИ
         *
         * УДОЧКА НЕ ЗАБРОСИТСЯ,
         * ЕСЛИ В ЕЁ СЛОТЕ НЕТ ПРИМАНКИ
         * =====================================================
         */

        ItemStack bait =
                RodBaitData.getBait(
                        itemStack
                );


        if (
                bait.isEmpty()
        ) {

            if (
                    level.isClientSide
            ) {

                player.displayClientMessage(

                        Component.translatable(
                                        "message.fisch.need_bait"
                                )
                                .withStyle(
                                        ChatFormatting.RED
                                ),

                        true
                );
            }


            return InteractionResultHolder.fail(
                    itemStack
            );
        }


        /*
         * =====================================================
         * ЗАБРАСЫВАЕМ УДОЧКУ
         * =====================================================
         */

        level.playSound(
                null,

                player.getX(),
                player.getY(),
                player.getZ(),

                SoundEvents.FISHING_BOBBER_THROW,

                SoundSource.NEUTRAL,

                0.5F,

                0.4F /
                        (
                                level.getRandom()
                                        .nextFloat()
                                        * 0.4F
                                        + 0.8F
                        )
        );


        if (
                !level.isClientSide
        ) {

            int luckBonus =
                    EnchantmentHelper
                            .getFishingLuckBonus(
                                    itemStack
                            );


            int speedBonus =
                    EnchantmentHelper
                            .getFishingSpeedBonus(
                                    itemStack
                            );


            level.addFreshEntity(
                    new FishingHook(
                            player,
                            level,
                            luckBonus,
                            speedBonus
                    )
            );
        }


        player.awardStat(
                Stats.ITEM_USED.get(
                        this
                )
        );


        player.gameEvent(
                GameEvent.ITEM_INTERACT_START
        );


        return InteractionResultHolder.sidedSuccess(
                itemStack,
                level.isClientSide()
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.literal(""));

        tooltip.add(Component.literal("Luck: " + (int) this.luck + "%")
                .withStyle(ChatFormatting.GOLD));

        tooltip.add(Component.literal("Control: " + this.control)
                .withStyle(ChatFormatting.AQUA));

        tooltip.add(Component.literal("Resilience: " + (int) (this.resilience * 100)  + "%")
                .withStyle(ChatFormatting.GREEN));

        super.appendHoverText(stack, level, tooltip, flag);
    }


    /*
     * =====================================================
     * GETTERS
     * =====================================================
     */

    public float getLuck() {

        return luck;
    }


    public float getControl() {

        return control;
    }


    public float getResilience() {

        return resilience;
    }


    /*
     * =====================================================
     * RESILIENCE
     * =====================================================
     */

    public static float getResilienceMultiplier(
            float resilience
    ) {

        float min =
                0.01F;

        float max =
                1.0F;


        resilience =
                Math.max(
                        min,

                        Math.min(
                                max,
                                resilience
                        )
                );


        return 1.0F -
                resilience *
                        0.5F;
    }



}