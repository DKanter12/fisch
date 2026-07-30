package com.fisch.mixin;

import com.fisch.FischMod;
import com.mojang.datafixers.util.Either;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SinglePoolElement.class)
public class SinglePoolElementMixin {

    @Shadow
    protected Either<ResourceLocation, StructureTemplate> template;

    @ModifyVariable(method = "getSettings", at = @At("RETURN"), ordinal = 0)
    private StructurePlaceSettings fisch$disableLiquids(StructurePlaceSettings settings) {
        template.ifLeft(location -> {
            if (location.equals(new ResourceLocation(FischMod.MODID, "statue_of_sovereignty"))) {
                settings.setKeepLiquids(false);
            }
        });
        return settings;
    }
}
