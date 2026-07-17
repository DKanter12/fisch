package com.fisch.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class FishMongerModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("fisch", "fishmongermodel"), "main");
    private final ModelPart headwear;
    private final ModelPart body;
    private final ModelPart bodywear;
    private final ModelPart arms;
    private final ModelPart right_leg;
    private final ModelPart left_leg;
    private final ModelPart headwear2;
    private final ModelPart head;

    public FishMongerModel(ModelPart root) {
        this.headwear = root.getChild("headwear");
        this.body = root.getChild("body");
        this.bodywear = root.getChild("bodywear");
        this.arms = root.getChild("arms");
        this.right_leg = root.getChild("right_leg");
        this.left_leg = root.getChild("left_leg");
        this.headwear2 = root.getChild("headwear2");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(73, 42).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.51F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(67, 40).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bodywear = partdefinition.addOrReplaceChild("bodywear", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.5F))
                .texOffs(116, 11).addBox(4.0F, 11.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(116, 11).addBox(-6.0F, 11.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        bodywear.addOrReplaceChild("leska3_r1", CubeListBuilder.create().texOffs(91, 107).addBox(-2.0F, -14.0F, 2.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 7.0F, 5.0F, -0.0209F, 0.0358F, -0.4731F));
        bodywear.addOrReplaceChild("leska2_r1", CubeListBuilder.create().texOffs(82, 121).addBox(-1.9612F, -12.4966F, 3.9569F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(97, 106).addBox(-0.182F, -13.5868F, 3.0397F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(91, 107).addBox(-1.2452F, -6.8803F, 4.1773F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(122, 102).addBox(-1.0358F, -8.0209F, 0.9991F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(124, 86).addBox(-1.0F, -14.0F, 1.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(114, 45).addBox(-3.0F, -6.0F, 1.0F, 4.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, 3.0F, -0.0209F, 0.0358F, -0.4731F));

        PartDefinition arms = partdefinition.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.95F, -1.05F, -0.7505F, 0.0F, 0.0F));

        // ИСПРАВЛЕНО: 'mirrored' теперь является частью 'arms' и не вызывает рекурсивного переполнения при рендере
        arms.addOrReplaceChild("mirrored", CubeListBuilder.create().texOffs(44, 22).mirror().addBox(4.0F, -23.05F, -3.05F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 21.05F, 1.05F));

        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(118, 0).addBox(-2.0F, 10.0F, -3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(103, 0).addBox(-2.0F, 10.0F, -3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("headwear2", CubeListBuilder.create().texOffs(-4, 79).addBox(-7.0F, -6.0F, -8.0F, 14.0F, 0.01F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(56, 109).addBox(-4.0F, -10.0F, 4.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(54, 81).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(56, 109).addBox(-4.0F, -10.0F, -5.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 102).addBox(4.0F, -10.0F, -4.0F, 1.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(59, 102).addBox(-5.0F, -10.0F, -4.0F, 1.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(62, 89).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 2.0F, 0.01F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.headwear.yRot = this.head.yRot;
        this.headwear.xRot = this.head.xRot;
        this.headwear2.yRot = this.head.yRot;
        this.headwear2.xRot = this.head.xRot;
        this.right_leg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
        this.left_leg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount * 0.5F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        headwear.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        bodywear.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        arms.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        headwear2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}