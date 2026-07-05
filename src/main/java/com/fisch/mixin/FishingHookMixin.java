package com.fisch.mixin;

import com.fisch.FischMod;
import com.fisch.FishingHookDuck;
import com.fisch.item.ModItems;
import com.fisch.rod.NewRod;
import com.fisch.rod.RodMechanics;
import com.fisch.fish.NewFish;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.fisch.FischMod.LOGGER;
import static com.fisch.FischMod.MODID;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin implements FishingHookDuck {

    @Unique
    private static final Logger fisch$LOGGER = LoggerFactory.getLogger("FischMod");

    @Shadow
    public abstract Player getPlayerOwner();

    @Shadow
    private int nibble; // Ванильный таймер поклёвки (сколько тиков рыба будет на крючке)

    // Наше кастомное поле для хранения определенной рыбы
    @Unique
    private NewFish fisch$customCatch = null;

    // Флаг, гарантирующий, что поклёвка началась и рыба все еще считается активной для подсечки
    @Unique
    private boolean fisch$isBiting = false;

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

        if (hook.level().isClientSide()) {
            return;
        }

        if (this.nibble > 0 && !this.fisch$isBiting) {

            Player player = this.getPlayerOwner();

            if (player == null) {
                return;
            }

            this.fisch$isBiting = true;

            String bait = getBaitFromPlayer(player);
            ItemStack itemStack = player.getMainHandItem();

            if (itemStack.getItem() instanceof NewRod newRod) {
                NewRod rod = (NewRod) itemStack.getItem();
                this.fisch$customCatch = RodMechanics.determineCatch(
                        hook.level(),
                        getActiveBestiary(),
                        bait,
                        rod.getLuck()
                );
            }

            if (itemStack.getItem() instanceof FishingRodItem){
                this.fisch$customCatch = RodMechanics.determineCatch(
                        hook.level(),
                        getActiveBestiary(),
                        bait,
                        1f
                );
            }


            if (this.fisch$customCatch == null) {
                return;
            }

            if (player instanceof ServerPlayer serverPlayer) {

                FriendlyByteBuf buf = PacketByteBufs.create();

                buf.writeUtf(this.fisch$customCatch.name);
                buf.writeInt(this.fisch$customCatch.rarity);

                if (itemStack.getItem() instanceof NewRod newRod) {
                    buf.writeFloat(newRod.getControl());
                    buf.writeFloat(newRod.getResilience());
                    buf.writeFloat(newRod.getLuck());
                } else {
                    buf.writeFloat(0.001F);
                    buf.writeFloat(0.001f);
                    buf.writeFloat(0.001f);
                }

                ServerPlayNetworking.send(
                        serverPlayer,
                        FischMod.FISH_GUI_PACKET_ID,
                        buf
                );
            }

            LOGGER.info("Миниигра началась.");
        }
    }

    /**
     * Перехват вылавливания рыбы (нажатие ПКМ игроком).
     * Мы перехватываем HEAD метода retrieve.
     */


    @Unique
    private String getBaitFromPlayer(Player player) {
        ItemStack rod = player.getMainHandItem();
        if (rod.getItem() instanceof net.minecraft.world.item.FishingRodItem) {
            if (rod.hasTag() && rod.getTag().contains("Bait")) {
                return rod.getTag().getString("Bait");
            }
        }
        return "none";
    }

    @Override
    public void finishMiniGame(boolean success) {

        FishingHook hook = (FishingHook) (Object) this;

        if (hook.level().isClientSide()) {
            return;
        }

        Player player = getPlayerOwner();

        if (player == null) {
            hook.discard();
            return;
        }

        if (success && this.fisch$customCatch != null) {
            giveCustomFishToPlayer(player, this.fisch$customCatch);
        }

        this.fisch$isBiting = false;
        this.fisch$customCatch = null;

        player.fishing = null;
        hook.discard();
    }

    @Unique
    private NewFish[] getActiveBestiary() {
        return ModItems.ALL_FISH;
    }

    @Unique
    private void giveCustomFishToPlayer(Player player, NewFish fish) {
        fisch$LOGGER.info("Игрок " + player.getName().getString() + " выловил кастомную рыбу: " + fish.name);

        ItemStack rod = player.getMainHandItem();
        Item rodItem = rod.getItem();
        Item item = BuiltInRegistries.ITEM.get(
                ResourceLocation.tryParse(MODID + ":" + fish.name)
        );
        player.addItem(new ItemStack(item));

    }

}