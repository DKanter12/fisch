package com.fisch.mixin;

import com.fisch.FischMod;
import com.fisch.FishingHookDuck;
import com.fisch.fish.FishMutation;
import com.fisch.fish.NewFish;
import com.fisch.fish.Relic;
import com.fisch.item.Bait;
import com.fisch.item.ModItems;
import com.fisch.rod.NewFishingRod;
import com.fisch.rod.RodBaitData;
import com.fisch.rod.RodEnchantment;
import com.fisch.rod.RodMechanics;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin implements FishingHookDuck {

    @Shadow
    public abstract Player getPlayerOwner();

    @Shadow
    private int nibble;

    @Shadow
    private int timeUntilLured;

    @Unique
    private NewFish fisch$customCatch;

    @Unique
    private boolean fisch$isBiting;

    @Unique
    private static final Logger fisch$LOGGER = LoggerFactory.getLogger("FischMod");

    @Unique
    private static final Random fisch$RANDOM = new Random();

    @Override
    public NewFish getCustomCatch() {
        return this.fisch$customCatch;
    }

    @Override
    public void setCustomCatch(NewFish fish) {
        this.fisch$customCatch = fish;
    }

    @Redirect(
            method = "shouldStopFishing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"
            )
    )
    private boolean fischAllowCustomRod(ItemStack stack, Item item) {
        if (item == Items.FISHING_ROD) {
            return stack.getItem() instanceof FishingRodItem;
        }
        return stack.is(item);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyBaitTickEffects(CallbackInfo ci) {
        if (this.fisch$isBiting) {
            this.nibble = 0;
        }

        FishingHook hook = (FishingHook) (Object) this;
        if (!hook.level().isClientSide() && this.timeUntilLured > 0) {
            Player player = this.getPlayerOwner();
            if (player != null) {
                String bait = getBaitFromPlayer(player);
                if (bait.equals("bait_blend") && hook.tickCount % 2 == 0) {
                    this.timeUntilLured--;
                } else if (bait.equals("black_fish_eggs") && hook.tickCount % 2 == 0) {
                    this.timeUntilLured++;
                }
            }
        }
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/projectile/FishingHook;nibble:I",
                    shift = At.Shift.AFTER
            )
    )
    private void onBiteStart(CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;

        if (hook.level().isClientSide()) return;
        if (this.nibble <= 0) return;
        if (this.fisch$isBiting) return;

        Player player = this.getPlayerOwner();
        if (player == null) return;

        this.fisch$isBiting = true;

        ItemStack rodStack = player.getMainHandItem();
        String bait = getBaitFromPlayer(player);
        ItemStack baitStack = RodBaitData.getBait(rodStack);

        // -- ДЕБАФФ CRAB CLAW --
        if (bait.equals("crab_claw") && fisch$RANDOM.nextInt(100) < 15) {
            this.fisch$isBiting = false;
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("message.fisch.line_snapped").withStyle(ChatFormatting.RED));
            }

            if (!baitStack.isEmpty()) {
                baitStack.shrink(1);
                RodBaitData.setBait(rodStack, baitStack);
            }
            return;
        }

        // -- ИСПРАВЛЕННЫЙ ПОДСЧЕТ УДАЧИ --
        float extraLuck = 0.0f;
        if (baitStack.getItem() instanceof Bait baitItem) {
            extraLuck = baitItem.getLuckBonus();
        }

        float baseLuck = 0.0f; // ИСПРАВЛЕНО: Было 1.0f, из-за чего деф удочка была имбовой
        float enchLuck = 0.0f;

        if (rodStack.getItem() instanceof NewFishingRod newRod) {
            baseLuck = newRod.getLuck();
            String ench = RodEnchantment.getEnchantment(rodStack);
            enchLuck = RodEnchantment.getLuckBonus(ench);
        }

        float totalLuck = baseLuck + enchLuck + extraLuck;

        // -- ИСПРАВЛЕННАЯ СИСТЕМА ВЫБОРА РЫБЫ --
        // Раньше было N ре-роллов с выбором лучшей (минимальной) редкости,
        // из-за чего даже на деф удочке экзотики сыпались бесконечно.
        // Теперь рыба выбирается ОДНИМ взвешенным броском: удочка
        // с большей удачей просто сильнее увеличивает вес редких рыб
        // в fishDropPercentage, а экзотика остаётся редкостью.
        this.fisch$customCatch = RodMechanics.determineCatch(
                hook.level(),
                hook.blockPosition(),
                getActiveBestiary(),
                bait,
                totalLuck
        );

        // Тратим приманку
        if (!baitStack.isEmpty()) {
            baitStack.shrink(1);
            RodBaitData.setBait(rodStack, baitStack);
        }

        if (this.fisch$customCatch == null) {
            this.fisch$isBiting = false;
            return;
        }

        // Запуск мини-игры
        if (player instanceof ServerPlayer serverPlayer) {
            FriendlyByteBuf buffer = PacketByteBufs.create();

            buffer.writeUtf(this.fisch$customCatch.name);
            buffer.writeInt(this.fisch$customCatch.rarity);

            if (rodStack.getItem() instanceof NewFishingRod newRod) {
                String ench = RodEnchantment.getEnchantment(rodStack);
                float control = newRod.getControl() + RodEnchantment.getControlBonus(ench);
                float resilience = newRod.getResilience() + RodEnchantment.getResilienceBonus(ench);

                if (bait.equals("crab_claw")) {
                    control *= 1.30f;
                } else if (bait.equals("sea_cucumber")) {
                    control *= 0.80f;
                    resilience *= 0.80f;
                }

                buffer.writeFloat(control);
                buffer.writeFloat(resilience);
                buffer.writeFloat(totalLuck);
            } else {
                buffer.writeFloat(0.001F);
                buffer.writeFloat(0.001F);
                buffer.writeFloat(0.001F);
            }

            ServerPlayNetworking.send(serverPlayer, FischMod.FISH_GUI_PACKET_ID, buffer);
        }
        fisch$LOGGER.info("Мини-игра началась.");
    }

    @Unique
    private String getBaitFromPlayer(Player player) {
        ItemStack rod = player.getMainHandItem();
        if (rod.getItem() instanceof FishingRodItem) {
            if (rod.hasTag() && rod.getTag().contains("Bait")) {
                return rod.getTag().getString("Bait");
            }
        }
        return "none";
    }

    @Unique
    private NewFish[] getActiveBestiary() {
        FishingHook hook = (FishingHook) (Object) this;
        ResourceKey<Biome> biomeKey = hook.level().getBiome(hook.blockPosition()).unwrapKey().orElse(null);
        if (biomeKey == null) return ModItems.PLAIN_FISH;
        String biomeId = biomeKey.location().getPath();

        if (biomeId.equals("snowy_plains") || biomeId.equals("ice_spikes") || biomeId.equals("snowy_taiga")
                || biomeId.equals("frozen_river") || biomeId.equals("frozen_ocean") || biomeId.equals("deep_frozen_ocean")
                || biomeId.equals("cold_ocean") || biomeId.equals("deep_cold_ocean") || biomeId.equals("snowy_beach")
                || biomeId.equals("grove") || biomeId.equals("snowy_slopes") || biomeId.equals("frozen_peaks")
                || biomeId.equals("jagged_peaks")) {
            return ModItems.ICE_FISH;
        }

        if (biomeId.equals("desert") || biomeId.equals("badlands")
                || biomeId.equals("eroded_badlands") || biomeId.equals("wooded_badlands")) {
            return ModItems.DESERT_FISH;
        }

        if (biomeId.equals("jungle") || biomeId.equals("sparse_jungle") || biomeId.equals("bamboo_jungle")) {
            return ModItems.JUNGLE_FISH;
        }

        if (biomeId.equals("swamp") || biomeId.equals("mangrove_swamp")) {
            return ModItems.SWAMP_FISH;
        }

        if (biomeId.contains("mushroom")) {
            return ModItems.MUSHROOM_FISH;
        }

        return ModItems.PLAIN_FISH;
    }

    @Override
    public void finishMiniGame(boolean success) {
        FishingHook hook = (FishingHook) (Object) this;

        if (hook.level().isClientSide()) return;

        Player player = getPlayerOwner();

        if (player == null) {
            hook.discard();
            return;
        }

        if (success && this.fisch$customCatch != null) {
            ItemStack fishStack = new ItemStack(this.fisch$customCatch);

            if (this.fisch$customCatch instanceof Relic) {
                Relic.setRandomEnchantment(fishStack, hook.level().getRandom());
            }

            int roll = fisch$RANDOM.nextInt(100);
            if (roll < 10) {
                FishMutation.applyMutation(fishStack, FishMutation.SHINY);
            } else if (roll < 20) {
                FishMutation.applyMutation(fishStack, FishMutation.SPARKLING);
            }

            ItemEntity fishEntity = new ItemEntity(hook.level(), hook.getX(), hook.getY(), hook.getZ(), fishStack);
            fishEntity.setPickUpDelay(10);
            Vec3 direction = player.position().add(0, player.getEyeHeight(), 0).subtract(hook.position()).normalize();
            fishEntity.setDeltaMovement(direction.x * 0.35, 0.45, direction.z * 0.35);
            hook.level().addFreshEntity(fishEntity);

            String bait = getBaitFromPlayer(player);

            if (bait.equals("black_fish_eggs") && fisch$RANDOM.nextInt(100) < 20) {
                ItemEntity extraFishEntity = new ItemEntity(hook.level(), hook.getX(), hook.getY(), hook.getZ(), fishStack.copy());
                extraFishEntity.setPickUpDelay(10);
                extraFishEntity.setDeltaMovement(direction.x * 0.35, 0.45, direction.z * 0.35);
                hook.level().addFreshEntity(extraFishEntity);
            }

            ItemStack rodStack = player.getMainHandItem();

            // -- ПАССИВКА ГРИБНОЙ УДОЧКИ: "Споровый улов" --
            // 25% шанс поймать копию рыбы (двойной улов).
            String rodPassive = "none";
            if (rodStack.getItem() instanceof NewFishingRod activeRod) {
                rodPassive = activeRod.getPassive();
            }

            if (rodPassive.equals("mushroom") && fisch$RANDOM.nextInt(100) < 25) {
                ItemEntity sporeFishEntity = new ItemEntity(hook.level(), hook.getX(), hook.getY(), hook.getZ(), fishStack.copy());
                sporeFishEntity.setPickUpDelay(10);
                sporeFishEntity.setDeltaMovement(direction.x * 0.35, 0.45, direction.z * 0.35);
                hook.level().addFreshEntity(sporeFishEntity);
            }

            // -- ПАССИВКА БОЛОТНОЙ УДОЧКИ: "Болотная живучесть" --
            // В болотных биомах приманка расходуется на 50% реже.
            boolean isSwampBiome = RodMechanics.getBiomeGroup(hook.level(), hook.blockPosition()).equals("swamp");
            if (rodPassive.equals("swamp") && isSwampBiome && fisch$RANDOM.nextInt(100) < 50) {
                ItemStack rodForBait = player.getMainHandItem();
                ItemStack rodBaitStack = RodBaitData.getBait(rodForBait);
                if (!rodBaitStack.isEmpty()) {
                    rodBaitStack.grow(1);
                    RodBaitData.setBait(rodForBait, rodBaitStack);
                }
            }

            int boxChance = 5;

            if (rodStack.getItem() == ModItems.MUSHROOM_ROD) {
                boxChance = 30;
            } else if (rodStack.getItem() == ModItems.SWAMP_ROD) {
                boxChance = 25;
            } else if (rodStack.getItem() == ModItems.JUNGLE_ROD) {
                boxChance = 20;
            } else if (rodStack.getItem() == ModItems.SAND_ROD) {
                boxChance = 10;
            } else if (rodStack.getItem() == ModItems.ICE_ROD) {
                boxChance = 8;
            }

            if (fisch$RANDOM.nextInt(100) < boxChance) {
                ItemStack boxStack = new ItemStack(ModItems.BAIT_BOX);
                ItemEntity boxEntity = new ItemEntity(hook.level(), hook.getX(), hook.getY(), hook.getZ(), boxStack);
                boxEntity.setPickUpDelay(10);
                boxEntity.setDeltaMovement(direction.x * 0.35, 0.45, direction.z * 0.35);
                hook.level().addFreshEntity(boxEntity);
            }

            if (!RodMechanics.isValidWaterBody(hook.level(), hook.blockPosition())) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("message.fisch.small_water")));
                }
            }

            fisch$LOGGER.info("Игрок " + player.getName().getString() + " выловил кастомную рыбу: " + this.fisch$customCatch.name);
        }

        this.fisch$isBiting = false;
        this.fisch$customCatch = null;
        player.fishing = null;
        hook.discard();
    }
}