package com.fisch.client;

import com.fisch.FischMod;
import com.fisch.client.hud.CoinHudOverlay;
import com.fisch.client.model.FishMerchantModel;
import com.fisch.client.renderer.CustomVillagerRenderer;
import com.fisch.client.screen.BaitScreen;
import com.fisch.client.screen.FishCatchScreen;
import com.fisch.client.screen.FishMerchantScreen;
import com.fisch.command.ModCommands;
import com.fisch.registry.ModMenuTypes;
import com.fisch.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import static com.fisch.FischMod.MODID;

public class FischModClient implements ClientModInitializer {

    public static final ResourceLocation FINISH_MINIGAME_PACKET =
            new ResourceLocation(MODID, "finish_minigame");

    @Override
    public void onInitializeClient() {

        MenuScreens.register(
                ModScreenHandlers.BAIT_MENU,
                BaitScreen::new
        );

        com.fisch.network.ModNetworkingClient.sendOpenBaitMenu();


        ClientPlayNetworking.registerGlobalReceiver(
                FischMod.FISH_GUI_PACKET_ID,
                (client, handler, buf, responseSender) -> {

                    String fishName = buf.readUtf();
                    int fishRarity = buf.readInt();
                    float control = buf.readFloat();
                    float resilience = buf.readFloat();
                    int rarity = buf.readInt();

                    client.execute(() ->
                            Minecraft.getInstance().setScreen(
                                    new FishCatchScreen(
                                            fishName,
                                            fishRarity,
                                            control,
                                            resilience
                                    )
                            )
                    );
                }
        );
        HudRenderCallback.EVENT.register(new CoinHudOverlay());
        ClientPlayNetworking.registerGlobalReceiver(
                new ResourceLocation("fisch", "money_sync"),
                (client, handler, buf, responseSender) -> {
                    long updatedBalance = buf.readLong();
                    client.execute(() -> {
                        ClientMoneyStorage.setBalance(updatedBalance);
                    });
                }
        );

        // Связываем логику с интерфейсом
        MenuScreens.register(ModMenuTypes.FISH_MERCHANT_MENU, FishMerchantScreen::new);

        // Регистрируем 3D модель самого жителя-рыбака
        EntityModelLayerRegistry.registerModelLayer(FishMerchantModel.LAYER_LOCATION, FishMerchantModel::createBodyLayer);

        // Переключатель рендера жителей
        EntityRendererRegistry.register(EntityType.VILLAGER, CustomVillagerRenderer::new);

        // Регистрация 3D-модели одежды
        EntityModelLayerRegistry.registerModelLayer(FishMerchantClothesModel.LAYER_LOCATION, FishMerchantClothesModel::createBodyLayer);

        // Добавляем одежду к стандартному жителю
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityType.VILLAGER && entityRenderer instanceof VillagerRenderer villagerRenderer) {
                registrationHelper.register(new FishMerchantLayer(villagerRenderer, context.getModelSet()));
            }
        });

        // ПРИМЕЧАНИЕ: ItemTooltipCallback (отображение цены при наведении) полностью удален!
        //ДИМА НЕ НАДО ОНО НЕНАДОООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООО
        //ГАНДОН
    }
}