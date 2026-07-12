package com.fisch.mixin;

import com.fisch.util.CurrencyHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin implements CurrencyHolder {
    private long fisch$money = 0;

    @Override
    public long getMoney() {
        return this.fisch$money;
    }

    @Override
    public void setMoney(long money) {
        this.fisch$money = money;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void injectWriteCustomDataToNbt(CompoundTag nbt, CallbackInfo ci) {
        nbt.putLong("fisch_money", this.fisch$money);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void injectReadCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("fisch_money")) {
            this.fisch$money = nbt.getLong("fisch_money");
        }
    }
}