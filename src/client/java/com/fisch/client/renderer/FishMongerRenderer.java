package com.fisch.client.renderer;

import com.fisch.client.model.FishMongerModel;
import com.fisch.entity.FishMongerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FishMongerRenderer extends MobRenderer<FishMongerEntity, FishMongerModel<FishMongerEntity>> {

    // Твоя новая текстура
    private static final ResourceLocation TEXTURE = new ResourceLocation("fisch", "textures/entity/fishmonger.png");

    public FishMongerRenderer(EntityRendererProvider.Context context) {
        super(context, new FishMongerModel<>(context.bakeLayer(FishMongerModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(FishMongerEntity entity) {
        return TEXTURE;
    }
}