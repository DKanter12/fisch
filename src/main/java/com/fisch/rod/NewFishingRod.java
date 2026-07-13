package com.fisch.rod;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class NewFishingRod extends FishingRodItem {

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

        this.luck = luck;
        this.control = control;
        this.resilience = resilience;
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

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (player.fishing != null) {
            if (!level.isClientSide) {
                int i = player.fishing.retrieve(itemStack);
                itemStack.hurtAndBreak(i, player, (playerx) -> playerx.broadcastBreakEvent(interactionHand));
            }

            level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!level.isClientSide) {
                int i = EnchantmentHelper.getFishingSpeedBonus(itemStack);
                int j = EnchantmentHelper.getFishingLuckBonus(itemStack);
                level.addFreshEntity(new FishingHook(player, level, j, i));
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            player.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    public static float getResilienceMultiplier(float resilience) {

        float min = 0.01f;
        float max = 1.0f;

        resilience = Math.max(min, Math.min(max, resilience));

        return 1.0f - resilience * 0.5f;
    }
}