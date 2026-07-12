package com.fisch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerProfessionLayer.class)
public class VillagerProfessionLayerMixin {
    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void cancelFishermanVanillaClothes(PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Villager villager && villager.getVillagerData().getProfession() == VillagerProfession.FISHERMAN) {
            ci.cancel(); // Отменяем рендер ванильного фартука и шляпы рыбака
        }
    }
}