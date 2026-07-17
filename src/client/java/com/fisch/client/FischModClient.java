package com.fisch.client;

import com.fisch.FischMod;
import com.fisch.client.hud.CoinHudOverlay;
import com.fisch.client.model.FishMerchantModel;
import com.fisch.client.model.FishMongerModel;
import com.fisch.client.renderer.CustomVillagerRenderer;

import com.fisch.client.renderer.FishMongerRenderer;
import com.fisch.client.screen.FishCatchScreen;
import com.fisch.client.screen.FishMerchantScreen;
import com.fisch.client.screen.FishMongerScreen;
import com.fisch.entity.ModEntities;
import com.fisch.item.ModItems;
import com.fisch.registry.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;

import static com.fisch.FischMod.MODID;

public class FischModClient implements ClientModInitializer {

    public static final ResourceLocation FINISH_MINIGAME_PACKET = new ResourceLocation(MODID, "finish_minigame");

    @Override
    public void onInitializeClient() {
        // Рендеры существ
        EntityRendererRegistry.register(ModEntities.FISH_MONGER, FishMongerRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FishMongerModel.LAYER_LOCATION, FishMongerModel::createBodyLayer);

        // Регистрация кастов удочек
        registerCast(ModItems.ICE_ROD);
        registerCast(ModItems.SAND_ROD);
        registerCast(ModItems.JUNGLE_ROD);

        // Сетевые пакеты
        ClientPlayNetworking.registerGlobalReceiver(FischMod.FISH_GUI_PACKET_ID, (client, handler, buf, responseSender) -> {
            String fishName = buf.readUtf();
            int fishRarity = buf.readInt();
            float control = buf.readFloat();
            float resilience = buf.readFloat();
            int rarity = buf.readInt();

            client.execute(() -> Minecraft.getInstance().setScreen(new FishCatchScreen(fishName, fishRarity, control, resilience)));
        });

        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation(MODID, "money_sync"), (client, handler, buf, responseSender) -> {
            long updatedBalance = buf.readLong();
            client.execute(() -> ClientMoneyStorage.setBalance(updatedBalance));
        });

        // HUD и Меню
        HudRenderCallback.EVENT.register(new CoinHudOverlay());
        MenuScreens.register(ModMenuTypes.FISH_MONGER_MENU, FishMongerScreen::new);
        MenuScreens.register(ModMenuTypes.FISH_MERCHANT_MENU, FishMerchantScreen::new);

        // Модели и слои
        EntityModelLayerRegistry.registerModelLayer(FishMerchantModel.LAYER_LOCATION, FishMerchantModel::createBodyLayer);
        EntityRendererRegistry.register(EntityType.VILLAGER, CustomVillagerRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FishMerchantClothesModel.LAYER_LOCATION, FishMerchantClothesModel::createBodyLayer);

        // Одежда для жителей
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityType.VILLAGER && entityRenderer instanceof VillagerRenderer villagerRenderer) {
                registrationHelper.register(new FishMerchantLayer(villagerRenderer, context.getModelSet()));
            }
        });

        // ПРИМЕЧАНИЕ: Код ItemTooltipCallback полностью вырезан.
    }

    public static void registerCast(FishingRodItem rod) {
        ItemProperties.register(
                rod,
                new ResourceLocation("cast"),
                (stack, level, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    boolean mainHand = entity.getMainHandItem() == stack;
                    boolean offHand = entity.getOffhandItem() == stack;
                    if (entity.getMainHandItem().getItem() instanceof FishingRodItem) {
                        offHand = false;
                    }
                    return (mainHand || offHand) && entity instanceof Player player && player.fishing != null ? 1.0F : 0.0F;
                }
        );
    }
}