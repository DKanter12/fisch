    package com.fisch.mixin; // Убедись, что пакет соответствует твоей структуре

    import net.minecraft.client.model.VillagerModel;
    import net.minecraft.client.model.geom.ModelPart;
    import org.spongepowered.asm.mixin.Final;
    import org.spongepowered.asm.mixin.Mixin;
    import org.spongepowered.asm.mixin.Shadow;

    @Mixin(VillagerModel.class)
    public class VillagerModelMixin {
        // Тенируем все части, чтобы они были доступны для использования
        @Shadow @Final private ModelPart root;
        @Shadow @Final private ModelPart head;
        @Shadow @Final private ModelPart nose;
        @Shadow @Final private ModelPart hat;
        @Shadow @Final private ModelPart hatRim;
        @Shadow @Final private ModelPart rightLeg;
        @Shadow @Final private ModelPart leftLeg;

    }