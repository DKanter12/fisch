package com.fisch.rod;

import com.fisch.FischMod;
import com.fisch.item.Bait;
import com.fisch.screen.BaitScreenHandler;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class NewRod extends FishingRodItem {

    private final float luck;
    private final float control;
    private final float resilience;

    public NewRod(
            Properties properties,
            float luck,
            float control,
            float resilience
    ) {
        super(properties);

        this.luck = luck;
        this.control = control;
        this.resilience = resilience;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand interactionHand
    ) {
        FischMod.LOGGER.info("EFJFE4PIO");

        ItemStack itemStack =
                player.getItemInHand(interactionHand);

        /*
         * SHIFT + ПКМ
         * Открываем меню приманки
         */


        int i;

        /*
         * Если поплавок уже заброшен —
         * забираем его
         */
        if (player.fishing != null) {

            if (!level.isClientSide) {

                i =
                        player.fishing.retrieve(
                                itemStack
                        );

                itemStack.hurtAndBreak(
                        i,
                        player,
                        p ->
                                p.broadcastBreakEvent(
                                        interactionHand
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
        }

        /*
         * Забрасываем удочку
         */
        else {

            ItemStack rightHand =
                    player.getMainHandItem();

            /*
             * Пока оставляем твою старую проверку
             */
            if (!(rightHand.getItem() instanceof Bait)) {

                return InteractionResultHolder.fail(
                        itemStack
                );
            }

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

            if (!level.isClientSide) {

                i =
                        EnchantmentHelper
                                .getFishingSpeedBonus(
                                        itemStack
                                );

                int j =
                        EnchantmentHelper
                                .getFishingLuckBonus(
                                        itemStack
                                );

                level.addFreshEntity(
                        new FishingHook(
                                player,
                                level,
                                j,
                                i
                        )
                );
            }

            player.awardStat(
                    Stats.ITEM_USED.get(this)
            );

            player.gameEvent(
                    GameEvent.ITEM_INTERACT_START
            );
        }

        return InteractionResultHolder.sidedSuccess(
                itemStack,
                level.isClientSide()
        );
    }

    public float getLuck() {
        return luck;
    }

    public float getControl() {
        return control;
    }

    public float getResilience() {
        return resilience;
    }

    public static float getResilienceMultiplier(
            float resilience
    ) {

        float min = 0.01f;
        float max = 1.0f;

        resilience =
                Math.max(
                        min,
                        Math.min(
                                max,
                                resilience
                        )
                );

        return 1.0f - resilience * 0.5f;
    }
}