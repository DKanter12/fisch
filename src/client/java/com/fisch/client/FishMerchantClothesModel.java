package com.fisch.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class FishMerchantClothesModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("fisch", "fish_merchant_clothes"), "main");
    private final ModelPart bodywear;
    private final ModelPart headwear2;

    public FishMerchantClothesModel(ModelPart root) {
        this.bodywear = root.getChild("bodywear");
        this.headwear2 = root.getChild("headwear2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();


        // Оставляем ТОЛЬКО твою одежду, которую ты создал в Blockbench
        partdefinition.addOrReplaceChild("bodywear", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.5F))
                .texOffs(116, 11).addBox(4.0F, 11.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(116, 11).addBox(-6.0F, 11.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 125).addBox(2.0F, 0.0F, 3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 125).addBox(1.0F, 1.0F, 3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(15, 125).addBox(0.0F, 2.0F, 3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(114, 121).addBox(-4.0F, 4.0F, 3.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(95, 122).addBox(-4.0F, 5.0F, 4.0F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(109, 110).addBox(-4.0F, 10.0F, 3.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(109, 110).addBox(-4.0F, 6.0F, 5.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(109, 110).addBox(-4.0F, 10.0F, 4.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(111, 109).addBox(-5.0F, 6.0F, 3.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(112, 110).addBox(0.0F, 10.0F, 3.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("headwear2", CubeListBuilder.create().texOffs(0, 82).addBox(-6.0F, -32.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 99).addBox(-5.0F, -34.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(54, 109).addBox(-6.0F, -31.0F, 6.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(54, 81).addBox(-4.0F, -35.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(54, 109).addBox(-6.0F, -31.0F, -7.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(43, 98).addBox(6.0F, -31.0F, -6.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(43, 98).addBox(-7.0F, -31.0F, -6.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bodywear.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        headwear2.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    // Добавь это внутрь класса FishMerchantClothesModel перед самой последней закрывающей скобкой }
    public ModelPart getBodywear() {
        return this.bodywear;
    }

    public ModelPart getHeadwear2() {
        return this.headwear2;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}
}