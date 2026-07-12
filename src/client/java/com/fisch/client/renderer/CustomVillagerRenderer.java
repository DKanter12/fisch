package com.fisch.client.renderer;

import com.fisch.client.model.FishMerchantModel; // <-- Добавлен импорт твоей новой модели
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class CustomVillagerRenderer extends EntityRenderer<Villager> {
    private final VillagerRenderer vanillaRenderer;
    private final FishermanRenderer fishermanRenderer;

    public CustomVillagerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.vanillaRenderer = new VillagerRenderer(context);
        this.fishermanRenderer = new FishermanRenderer(context);
    }

    @Override
    public void render(Villager entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.getVillagerData().getProfession() == VillagerProfession.FISHERMAN) {
            this.fishermanRenderer.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        } else {
            this.vanillaRenderer.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Villager entity) {
        if (entity.getVillagerData().getProfession() == VillagerProfession.FISHERMAN) {
            return this.fishermanRenderer.getTextureLocation(entity);
        }
        return this.vanillaRenderer.getTextureLocation(entity);
    }

    // ИСПРАВЛЕНО: Заменили старое название FishermanModel228 на FishMerchantModel
    private static class FishermanRenderer extends MobRenderer<Villager, FishMerchantModel<Villager>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation("fisch", "textures/entity/fisherman.png");

        public FishermanRenderer(EntityRendererProvider.Context context) {
            super(context, new FishMerchantModel<>(context.bakeLayer(FishMerchantModel.LAYER_LOCATION)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(Villager entity) {
            return TEXTURE;
        }
    }
}