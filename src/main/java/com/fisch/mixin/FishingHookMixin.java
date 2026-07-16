package com.fisch.mixin;

import com.fisch.FischMod;
import com.fisch.FishingHookDuck;
import com.fisch.item.ModItems;
import com.fisch.rod.NewRod;
import com.fisch.rod.RodMechanics;
import com.fisch.fish.NewFish;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
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

@Mixin(FishingHook.class)
public abstract class FishingHookMixin implements FishingHookDuck {
    @Unique
    private static final Logger fisch$LOGGER = LoggerFactory.getLogger("FischMod");

    @Shadow public abstract Player getPlayerOwner();
    @Shadow private int nibble;
    @Shadow private int timeUntilLured;

    @Unique private NewFish fisch$customCatch = null;
    @Unique private boolean fisch$isBiting = false;
    @Unique private boolean fisch$warnedSmallWater = false;

    @Override
    public NewFish getCustomCatch() { return this.fisch$customCatch; }

    @Override
    public void setCustomCatch(NewFish fish) { this.fisch$customCatch = fish; }

    @Redirect(
            method = "shouldStopFishing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean fischAllowCustomRod(ItemStack stack, Item item) {
        if (item == Items.FISHING_ROD) return stack.getItem() instanceof FishingRodItem;
        return stack.is(item);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void fisch$checkPuddles(CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;

        // ДОБАВЛЕНО: && !this.fisch$isBiting — если миниигра уже идет, проверку на размер лужи пропускаем
        if (!hook.level().isClientSide() && this.timeUntilLured > 0 && !this.fisch$isBiting) {
            if (!RodMechanics.isValidWaterBody(hook.level(), hook.blockPosition())) {
                if (!this.fisch$warnedSmallWater) {
                    Player player = this.getPlayerOwner();
                    if (player != null) {
                        player.displayClientMessage(Component.translatable("message.fisch.small_water"), true);
                    }
                    this.fisch$warnedSmallWater = true;
                }
                this.timeUntilLured = 100;
            }
        }
    }

    @Inject(
            method = "tick",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/FishingHook;nibble:I", shift = At.Shift.AFTER)
    )
    private void onBiteStart(CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;

        if (hook.level().isClientSide()) return;

        if (this.nibble > 0 && !this.fisch$isBiting) {
            Player player = this.getPlayerOwner();
            if (player == null) return;

            this.fisch$isBiting = true;
            String bait = getBaitFromPlayer(player);
            ItemStack itemStack = player.getMainHandItem();

            if (itemStack.getItem() instanceof NewRod newRod) {
                this.fisch$customCatch = RodMechanics.determineCatch(hook.level(), hook.blockPosition(), getActiveBestiary(), bait, newRod.getLuck());
            } else if (itemStack.getItem() instanceof FishingRodItem){
                this.fisch$customCatch = RodMechanics.determineCatch(hook.level(), hook.blockPosition(), getActiveBestiary(), bait, 1f);
            }

            if (this.fisch$customCatch == null) return;

            if (player instanceof ServerPlayer serverPlayer) {
                // ИСПРАВЛЕНО: Буфер создается локально под каждый конкретный пакет
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeUtf("");
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

                ServerPlayNetworking.send(serverPlayer, FischMod.FISH_GUI_PACKET_ID, buf);
            }
        }
    }

    @Unique
    private String getBaitFromPlayer(Player player) {
        ItemStack rod = player.getMainHandItem();
        if (rod.getItem() instanceof net.minecraft.world.item.FishingRodItem) {
            if (rod.hasTag() && rod.getTag().contains("Bait")) return rod.getTag().getString("Bait");
        }
        return "none";
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
            player.addItem(new ItemStack(this.fisch$customCatch));
        }

        this.fisch$isBiting = false;
        this.fisch$customCatch = null;
        this.fisch$warnedSmallWater = false;
        player.fishing = null;
        hook.discard();
    }

    @Unique
    private NewFish[] getActiveBestiary() {
        return ModItems.ALL_FISH;
    }
}