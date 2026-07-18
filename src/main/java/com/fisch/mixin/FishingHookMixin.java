package com.fisch.mixin;

import com.fisch.FischMod;
import com.fisch.FishingHookDuck;
import com.fisch.fish.NewFish;
import com.fisch.item.ModItems;
import com.fisch.rod.NewFishingRod;
import com.fisch.rod.RodBaitData;
import com.fisch.rod.RodMechanics;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

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

@Mixin(FishingHook.class)
public abstract class FishingHookMixin implements FishingHookDuck {

    @Shadow
    public abstract Player getPlayerOwner();

    @Shadow
    private int nibble;

    @Unique
    private NewFish fisch$customCatch;

    @Unique
    private boolean fisch$isBiting;

    @Unique
    private static final Logger fisch$LOGGER =
            LoggerFactory.getLogger("FischMod");


    /*
     * =========================================================
     * CUSTOM CATCH
     * =========================================================
     */

    @Override
    public NewFish getCustomCatch() {
        return this.fisch$customCatch;
    }

    @Override
    public void setCustomCatch(NewFish fish) {
        this.fisch$customCatch = fish;
    }


    /*
     * =========================================================
     * РАЗРЕШАЕМ КАСТОМНЫЕ УДОЧКИ
     * =========================================================
     */

    @Redirect(
            method = "shouldStopFishing",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/world/item/ItemStack;" +
                                    "is(Lnet/minecraft/world/item/Item;)Z"
            )
    )
    private boolean fischAllowCustomRod(
            ItemStack stack,
            Item item
    ) {
        if (item == Items.FISHING_ROD) {
            return stack.getItem() instanceof FishingRodItem;
        }

        return stack.is(item);
    }


    /*
     * =========================================================
     * ОТКЛЮЧАЕМ ВАНИЛЬНУЮ РЫБАЛКУ
     * =========================================================
     */

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void disableVanillaFishingDuringMiniGame(
            CallbackInfo ci
    ) {
        if (this.fisch$isBiting) {
            this.nibble = 0;
        }
    }


    /*
     * =========================================================
     * НАЧАЛО ПОКЛЁВКИ
     * =========================================================
     */

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target =
                            "Lnet/minecraft/world/entity/projectile/" +
                                    "FishingHook;nibble:I",
                    shift = At.Shift.AFTER
            )
    )
    private void onBiteStart(
            CallbackInfo ci
    ) {
        FishingHook hook =
                (FishingHook) (Object) this;


        /*
         * Работаем только на сервере
         */

        if (hook.level().isClientSide()) {
            return;
        }


        /*
         * Поклёвки нет
         */

        if (this.nibble <= 0) {
            return;
        }


        /*
         * Мини-игра уже запущена
         */

        if (this.fisch$isBiting) {
            return;
        }


        Player player =
                this.getPlayerOwner();


        if (player == null) {
            return;
        }


        this.fisch$isBiting = true;


        /*
         * Получаем удочку
         */

        ItemStack rodStack =
                player.getMainHandItem();


        /*
         * Получаем приманку
         */

        String bait =
                getBaitFromPlayer(player);


        /*
         * Определяем рыбу
         */

        if (
                rodStack.getItem()
                        instanceof NewFishingRod newRod
        ) {

            this.fisch$customCatch =
                    RodMechanics.determineCatch(
                            hook.level(),
                            hook.blockPosition(),
                            getActiveBestiary(),
                            bait,
                            newRod.getLuck()
                    );
        }

        else if (
                rodStack.getItem()
                        instanceof FishingRodItem
        ) {

            this.fisch$customCatch =
                    RodMechanics.determineCatch(
                            hook.level(),
                            hook.blockPosition(),
                            getActiveBestiary(),
                            bait,
                            1.0F
                    );
        }


        /*
         * Тратим одну приманку
         */

        ItemStack baitStack =
                RodBaitData.getBait(
                        rodStack
                );


        if (!baitStack.isEmpty()) {

            baitStack.shrink(1);

            RodBaitData.setBait(
                    rodStack,
                    baitStack
            );
        }


        /*
         * Если рыба не определилась
         */

        if (this.fisch$customCatch == null) {

            this.fisch$isBiting = false;

            return;
        }


        /*
         * Отправляем данные мини-игры клиенту
         */

        if (
                player instanceof ServerPlayer serverPlayer
        ) {

            FriendlyByteBuf buffer =
                    PacketByteBufs.create();


            /*
             * Название рыбы
             */

            buffer.writeUtf(
                    this.fisch$customCatch.name
            );


            /*
             * Редкость рыбы
             */

            buffer.writeInt(
                    this.fisch$customCatch.rarity
            );


            /*
             * Характеристики удочки
             */

            if (
                    rodStack.getItem()
                            instanceof NewFishingRod newRod
            ) {

                buffer.writeFloat(
                        newRod.getControl()
                );

                buffer.writeFloat(
                        newRod.getResilience()
                );

                buffer.writeFloat(
                        newRod.getLuck()
                );
            }

            else {

                buffer.writeFloat(0.001F);
                buffer.writeFloat(0.001F);
                buffer.writeFloat(0.001F);
            }


            /*
             * Отправляем пакет
             */

            ServerPlayNetworking.send(
                    serverPlayer,
                    FischMod.FISH_GUI_PACKET_ID,
                    buffer
            );
        }


        fisch$LOGGER.info(
                "Мини-игра началась."
        );
    }


    /*
     * =========================================================
     * ПОЛУЧАЕМ ПРИМАНКУ
     * =========================================================
     */

    @Unique
    private String getBaitFromPlayer(
            Player player
    ) {

        ItemStack rod =
                player.getMainHandItem();


        if (
                rod.getItem()
                        instanceof FishingRodItem
        ) {

            if (
                    rod.hasTag()
                            &&
                            rod.getTag()
                                    .contains("Bait")
            ) {

                return rod.getTag()
                        .getString("Bait");
            }
        }


        return "none";
    }


    /*
     * =========================================================
     * ВЫБОР БЕСТИАРИЯ ПО БИОМУ
     * =========================================================
     */

    @Unique
    private NewFish[] getActiveBestiary() {

        FishingHook hook =
                (FishingHook) (Object) this;


        ResourceKey<Biome> biomeKey =
                hook.level()
                        .getBiome(
                                hook.blockPosition()
                        )
                        .unwrapKey()
                        .orElse(null);


        if (biomeKey == null) {
            return ModItems.PLAIN_FISH;
        }


        String biomeId =
                biomeKey.location()
                        .getPath();


        /*
         * ЛЕДЯНЫЕ БИОМЫ
         */

        if (
                biomeId.equals("snowy_plains")
                        ||
                        biomeId.equals("ice_spikes")
                        ||
                        biomeId.equals("snowy_taiga")
                        ||
                        biomeId.equals("frozen_river")
                        ||
                        biomeId.equals("frozen_ocean")
                        ||
                        biomeId.equals("deep_frozen_ocean")
                        ||
                        biomeId.equals("cold_ocean")
                        ||
                        biomeId.equals("deep_cold_ocean")
                        ||
                        biomeId.equals("snowy_beach")
                        ||
                        biomeId.equals("grove")
                        ||
                        biomeId.equals("snowy_slopes")
                        ||
                        biomeId.equals("frozen_peaks")
                        ||
                        biomeId.equals("jagged_peaks")
        ) {

            return ModItems.ICE_FISH;
        }


        /*
         * ПУСТЫННЫЕ БИОМЫ
         */

        if (
                biomeId.equals("desert")
                        ||
                        biomeId.equals("badlands")
                        ||
                        biomeId.equals("eroded_badlands")
                        ||
                        biomeId.equals("wooded_badlands")
        ) {

            return ModItems.DESERT_FISH;
        }


        /*
         * ДЖУНГЛЕВЫЕ БИОМЫ
         */

        if (
                biomeId.equals("jungle")
                        ||
                        biomeId.equals("sparse_jungle")
                        ||
                        biomeId.equals("bamboo_jungle")
        ) {

            return ModItems.JUNGLE_FISH;
        }


        /*
         * ОБЫЧНЫЕ БИОМЫ
         */

        return ModItems.PLAIN_FISH;
    }


    /*
     * =========================================================
     * ЗАВЕРШЕНИЕ МИНИ-ИГРЫ
     * =========================================================
     */

    @Override
    public void finishMiniGame(
            boolean success
    ) {

        FishingHook hook =
                (FishingHook) (Object) this;


        /*
         * Работаем только на сервере
         */

        if (hook.level().isClientSide()) {
            return;
        }


        Player player =
                getPlayerOwner();


        if (player == null) {

            hook.discard();

            return;
        }


        /*
         * =====================================================
         * УСПЕШНАЯ ПОИМКА РЫБЫ
         * =====================================================
         */

        if (
                success
                        &&
                        this.fisch$customCatch != null
        ) {

            /*
             * Создаём предмет рыбы
             */

            ItemStack fishStack =
                    new ItemStack(
                            this.fisch$customCatch
                    );


            /*
             * Создаём ItemEntity
             */

            ItemEntity fishEntity =
                    new ItemEntity(
                            hook.level(),
                            hook.getX(),
                            hook.getY(),
                            hook.getZ(),
                            fishStack
                    );


            /*
             * Рыба некоторое время не подбирается
             */

            fishEntity.setPickUpDelay(
                    10
            );


            /*
             * Направление полёта к игроку
             */

            Vec3 direction =
                    player.position()
                            .add(
                                    0,
                                    player.getEyeHeight(),
                                    0
                            )
                            .subtract(
                                    hook.position()
                            )
                            .normalize();


            fishEntity.setDeltaMovement(
                    direction.x * 0.35,
                    0.45,
                    direction.z * 0.35
            );


            /*
             * =================================================
             * РЫБА ВЫЛЕТАЕТ ИЗ ВОДЫ
             * =================================================
             */

            hook.level()
                    .addFreshEntity(
                            fishEntity
                    );


            /*
             * =================================================
             * СООБЩЕНИЕ ПОСЛЕ ВЫЛЕТА РЫБЫ
             * =================================================
             *
             * Если водоём маленький,
             * сообщение появляется сразу после
             * появления рыбы в мире.
             *
             * Component.translatable()
             * автоматически использует язык игрока.
             */

            if (
                    !RodMechanics.isValidWaterBody(
                            hook.level(),
                            hook.blockPosition()
                    )
            ) {

                if (
                        player instanceof ServerPlayer serverPlayer
                ) {

                    serverPlayer.connection.send(
                            new ClientboundSetActionBarTextPacket(
                                    Component.translatable(
                                            "message.fisch.small_water"
                                    )
                            )
                    );
                }
            }


            fisch$LOGGER.info(
                    "Игрок "
                            + player.getName()
                            .getString()
                            + " выловил кастомную рыбу: "
                            + this.fisch$customCatch.name
            );
        }


        /*
         * =====================================================
         * СБРАСЫВАЕМ СОСТОЯНИЕ
         * =====================================================
         */

        this.fisch$isBiting = false;

        this.fisch$customCatch = null;

        player.fishing = null;

        hook.discard();
    }
}