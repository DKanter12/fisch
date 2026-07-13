package com.fisch.client;

import com.fisch.FischMod;
import com.fisch.client.hud.CoinHudOverlay;
import com.fisch.client.model.FishMerchantModel;
import com.fisch.client.renderer.CustomVillagerRenderer;
import com.fisch.client.screen.FishCatchScreen;
import com.fisch.client.screen.FishMerchantScreen;
import com.fisch.command.ModCommands;
import com.fisch.item.ModItems;
import com.fisch.registry.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.client.renderer.entity.FishingHookRenderer;


import static com.fisch.FischMod.MODID;
import net.minecraft.client.renderer.entity.FishingHookRenderer;

public class FischModClient implements ClientModInitializer {

    public static final ResourceLocation FINISH_MINIGAME_PACKET =
            new ResourceLocation(MODID, "finish_minigame");
    @Override
    public void onInitializeClient() {
        registerCast(ModItems.ICE_ROD);
        registerCast(ModItems.SAND_ROD);
        registerCast(ModItems.JUNGLE_ROD);

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

        // 1. САМОЕ ВАЖНОЕ ДЛЯ МЕНЮ: Связываем логику с интерфейсом!
        MenuScreens.register(ModMenuTypes.FISH_MERCHANT_MENU, FishMerchantScreen::new);

        // Регистрируем 3D модель самого жителя-рыбака
        EntityModelLayerRegistry.registerModelLayer(FishMerchantModel.LAYER_LOCATION, FishMerchantModel::createBodyLayer);

        // 2. Говорим игре использовать наш умный переключатель вместо стандартного рендера жителей
        EntityRendererRegistry.register(EntityType.VILLAGER, CustomVillagerRenderer::new);

        // Регистрация 3D-модели одежды
        EntityModelLayerRegistry.registerModelLayer(FishMerchantClothesModel.LAYER_LOCATION, FishMerchantClothesModel::createBodyLayer);

        // 3. Добавляем одежду к стандартному жителю
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityType.VILLAGER && entityRenderer instanceof VillagerRenderer villagerRenderer) {
                registrationHelper.register(new FishMerchantLayer(villagerRenderer, context.getModelSet()));
            }
        });

        // ==========================================
        // ОТОБРАЖЕНИЕ ЦЕНЫ В ИНВЕНТАРЕ
        // ==========================================
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (ModCommands.FISH_PRICES.containsKey(stack.getItem())) {
                int price = ModCommands.FISH_PRICES.get(stack.getItem());
                lines.add(Component.literal("§eЦена продажи: " + price + " C$"));
            }
        });
    }
    public static void registerCast(FishingRodItem rod){
        ItemProperties.register(
                rod,
                new ResourceLocation("cast"),
                (stack, level, entity, seed) -> {
                    if (entity == null) {
                        return 0.0F;
                    }

                    boolean mainHand = entity.getMainHandItem() == stack;
                    boolean offHand = entity.getOffhandItem() == stack;

                    if (entity.getMainHandItem().getItem() instanceof FishingRodItem) {
                        offHand = false;
                    }

                    return (mainHand || offHand) && entity instanceof Player player && player.fishing != null
                            ? 1.0F
                            : 0.0F;
                }
        );

    }
}