package com.fisch.client.renderer;

import com.fisch.client.model.FishermanWizardModel;
import com.fisch.entity.FishermanWizardEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FishermanWizardRenderer extends MobRenderer<FishermanWizardEntity, FishermanWizardModel<FishermanWizardEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("fisch", "textures/entity/fisherman_wizard.png");

    public FishermanWizardRenderer(EntityRendererProvider.Context context) {
        super(context, new FishermanWizardModel<>(context.bakeLayer(FishermanWizardModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(FishermanWizardEntity entity) {
        return TEXTURE;
    }
}