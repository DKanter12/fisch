package com.fisch.mixin;

import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerRenderer.class)
public class VillagerRendererMixin {
    private static final ResourceLocation CUSTOM_BASE = new ResourceLocation("fisch", "textures/entity/villager/villager.png");

    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/npc/Villager;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void setCustomFishermanBaseTexture(Villager villager, CallbackInfoReturnable<ResourceLocation> cir) {
        if (villager.getVillagerData().getProfession() == VillagerProfession.FISHERMAN) {
            cir.setReturnValue(CUSTOM_BASE); // Переключаем базовую текстуру кожи/глаз на твою
        }
    }
}