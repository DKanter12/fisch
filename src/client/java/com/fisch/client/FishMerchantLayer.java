package com.fisch.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class FishMerchantLayer extends RenderLayer<Villager, VillagerModel<Villager>> {
    private static final ResourceLocation CLOTHES_TEXTURE = new ResourceLocation("fisch", "textures/entity/fish_merchant_skin.png");
    private final FishMerchantClothesModel<Villager> model;

    public FishMerchantLayer(RenderLayerParent<Villager, VillagerModel<Villager>> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new FishMerchantClothesModel<>(modelSet.bakeLayer(FishMerchantClothesModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Villager entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.getVillagerData().getProfession() == VillagerProfession.FISHERMAN) {
            this.getParentModel().copyPropertiesTo(this.model);

            // Получаем ванильную голову и твою кастомную шляпу
            ModelPart vanillaHead = this.getParentModel().getHead();
            ModelPart customHat = this.model.getHeadwear2();

            // Намертво привязываем повороты кастомной шляпы к движениям головы жителя
            customHat.xRot = vanillaHead.xRot;
            customHat.yRot = vanillaHead.yRot;
            customHat.zRot = vanillaHead.zRot;

            renderColoredCutoutModel(this.model, CLOTHES_TEXTURE, poseStack, buffer, packedLight, entity, 1.0F, 1.0F, 1.0F);
        }
    }
}