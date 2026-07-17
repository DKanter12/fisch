package com.fisch.client;

import com.fisch.FischMod;
import com.fisch.client.hud.CoinHudOverlay;
import com.fisch.client.model.FishMerchantModel;
import com.fisch.client.model.FishMongerModel;
import com.fisch.client.renderer.CustomVillagerRenderer;
import com.fisch.client.renderer.FishMongerRenderer;
import com.fisch.client.screen.BaitScreen;
import com.fisch.client.screen.FishCatchScreen;
import com.fisch.client.screen.FishMerchantScreen;
import com.fisch.command.ModCommands;
import com.fisch.entity.ModEntities;
import com.fisch.item.ModItems;
import com.fisch.registry.ModMenuTypes;
import com.fisch.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;

import static com.fisch.FischMod.MODID;

public class FischModClient implements ClientModInitializer {

    public static final ResourceLocation FINISH_MINIGAME_PACKET =
            new ResourceLocation(MODID, "finish_minigame");

    @Override
    public void onInitializeClient() {
        registerCast(ModItems.ICE_ROD);
        registerCast(ModItems.SAND_ROD);
        registerCast(ModItems.JUNGLE_ROD);

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

        ServerPlayNetworking.registerGlobalReceiver(new ResourceLocation(FischMod.MODID, "buy_rod"), (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                // Проверяем, хватает ли у игрока денег (например, баланс хранится на сервере)
                // Допустим у тебя есть метод получения денег, например: long balance = PlayerMoneyProvider.getMoney(player);
                long price = 50;

                // Замени 'YOUR_MONEY_SYSTEM' на то, как у тебя на сервере списываются/проверяются монеты:
                if (/* баланс игрока >= price */ true) {
                    // Списываем деньги
                    // YOUR_MONEY_SYSTEM.takeMoney(player, price);

                    // Выдаем удочку (например, обычную бамбуковую или кастомную)
                    player.getInventory().add(new ItemStack(ModItems.ICE_ROD)); // Выдаем ледяную удочку как пример
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aВы успешно купили удочку!"));
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cНедостаточно монет!"));
                }
            });
        });
        // Проверь ошибку тут (см. Шаг 2 ниже)
        EntityRendererRegistry.register(ModEntities.FISH_MONGER, FishMongerRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FishMongerModel.LAYER_LOCATION, FishMongerModel::createBodyLayer);

        // ПРИМЕЧАНИЕ: ItemTooltipCallback (отображение цены при наведении) полностью удален!
        //ДИМА НЕ НАДО ОНО НЕНАДОООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООО
        //ГАНДОН
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