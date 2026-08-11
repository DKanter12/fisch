package com.fisch.client.renderer;

import com.fisch.FischMod;
import com.fisch.client.model.FishMongerNetherModel;
import com.fisch.entity.FishMongerNetherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FishMongerNetherRenderer extends MobRenderer<FishMongerNetherEntity, FishMongerNetherModel<FishMongerNetherEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FischMod.MODID, "textures/entity/fish_monger_nether.png");

    public FishMongerNetherRenderer(EntityRendererProvider.Context context) {
        super(context, new FishMongerNetherModel<>(context.bakeLayer(FishMongerNetherModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(FishMongerNetherEntity entity) {
        return TEXTURE;
    }
}