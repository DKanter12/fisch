package com.fisch.mixin;

import com.fisch.HomingItemDuck;
import com.fisch.fish.NewFish;
import com.fisch.item.ModItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements HomingItemDuck {

    @Unique
    private Player fisch$homingTarget;

    @Unique
    private int fisch$homingTicks;

    // Рыбы мода и коробки приманок не должны сгорать в лаве Ада.
    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void fisch$makeFishFireImmune(CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.getItem().getItem() instanceof NewFish || self.getItem().getItem() == ModItems.BAIT_BOX) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void fisch$enableHoming(Player target, int ticks) {
        this.fisch$homingTarget = target;
        this.fisch$homingTicks = Math.max(ticks, 1);
    }

    /*
     * Добыча с удочки всегда долетает до игрока: летит к нему по прямой,
     * а если не успела за отведённое время — сразу телепортируется к нему.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void fisch$steerToPlayer(CallbackInfo ci) {
        if (this.fisch$homingTarget == null || this.fisch$homingTicks <= 0) return;

        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide()) return;

        Player target = this.fisch$homingTarget;
        if (target == null || target.isRemoved()) {
            this.fisch$homingTarget = null;
            this.fisch$homingTicks = 0;
            return;
        }

        Vec3 pos = self.position();
        Vec3 tPos = target.position().add(0, target.getBbHeight() * 0.5, 0);

        this.fisch$homingTicks--;

        if (this.fisch$homingTicks <= 0 || pos.distanceToSqr(tPos) < 1.5 * 1.5) {
            // Долетела либо время вышло — кладём у игрока.
            self.setPos(tPos.x, tPos.y, tPos.z);
            self.setDeltaMovement(Vec3.ZERO);
            this.fisch$homingTarget = null;
            this.fisch$homingTicks = 0;
            return;
        }

        Vec3 dir = tPos.subtract(pos).normalize();
        double speed = 1.1;
        self.setDeltaMovement(dir.x * speed, Math.max(dir.y * speed, 0.25), dir.z * speed);
        self.setNoGravity(true);
    }
}
