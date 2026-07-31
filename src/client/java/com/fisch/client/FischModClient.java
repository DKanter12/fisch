package com.fisch.client;

import com.fisch.FischMod;
import com.fisch.client.hud.CoinHudOverlay;
import com.fisch.client.model.FishMerchantModel;
import com.fisch.client.model.FishMongerModel;
import com.fisch.client.model.FishermanWizardModel;
import com.fisch.client.renderer.CustomVillagerRenderer;
import com.fisch.client.renderer.FishMongerRenderer;
import com.fisch.client.renderer.FishermanWizardRenderer;
import com.fisch.client.screen.BaitScreen;
import com.fisch.client.screen.EnchantmentAltarScreen;
import com.fisch.client.screen.FishCatchScreen;
import com.fisch.client.screen.FishMerchantScreen;
import com.fisch.client.screen.FishMongerScreen;
import com.fisch.client.screen.WizardScreen;
import com.fisch.command.ModCommands;
import com.fisch.entity.ModEntities;
import com.fisch.fish.FishMutation;
import com.fisch.item.ModItems;
import com.fisch.registry.ModMenuTypes;
import com.fisch.screen.ModScreenHandlers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
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

        // --- РЕГИСТРАЦИЯ ЭКРАНОВ (SCREENS) ---
        MenuScreens.register(
                ModScreenHandlers.BAIT_MENU,
                BaitScreen::new
        );

        MenuScreens.register(
                ModScreenHandlers.ENCHANTMENT_ALTAR_MENU,
                EnchantmentAltarScreen::new
        );

        com.fisch.network.ModNetworkingClient.sendOpenBaitMenu();

        // --- ПАКЕТЫ И СЕТЬ (NETWORKING) ---
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

        // --- СВЯЗЬ GUI С MENU TYPES ---
        MenuScreens.register(ModMenuTypes.FISH_MERCHANT_MENU, FishMerchantScreen::new);
        MenuScreens.register(ModMenuTypes.FISH_MONGER_MENU, FishMongerScreen::new);
        MenuScreens.register(ModMenuTypes.WIZARD_MENU, WizardScreen::new);

        // --- РЕНДЕР И МОДЕЛИ (MODELS & RENDERERS) ---

        // 1. Обычный скупщик рыбы
        EntityModelLayerRegistry.registerModelLayer(FishMerchantModel.LAYER_LOCATION, FishMerchantModel::createBodyLayer);
        EntityRendererRegistry.register(EntityType.VILLAGER, CustomVillagerRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FishMerchantClothesModel.LAYER_LOCATION, FishMerchantClothesModel::createBodyLayer);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityType.VILLAGER && entityRenderer instanceof VillagerRenderer villagerRenderer) {
                registrationHelper.register(new FishMerchantLayer(villagerRenderer, context.getModelSet()));
            }
        });

        // 2. Продавец удочек
        EntityRendererRegistry.register(ModEntities.FISH_MONGER, FishMongerRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FishMongerModel.LAYER_LOCATION, FishMongerModel::createBodyLayer);

        // 3. Чародей (Fisherman Wizard)
        EntityRendererRegistry.register(ModEntities.FISHERMAN_WIZARD, FishermanWizardRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FishermanWizardModel.LAYER_LOCATION, FishermanWizardModel::createBodyLayer);

        // --- ПОКУПКА УДОЧКИ ---
        ServerPlayNetworking.registerGlobalReceiver(new ResourceLocation(FischMod.MODID, "buy_rod"), (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                long price = 50;

                if (/* баланс игрока >= price */ true) {
                    player.getInventory().add(new ItemStack(ModItems.ICE_ROD));
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aВы успешно купили удочку!"));
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cНедостаточно монет!"));
                }
            });
        });

        // ПРИМЕЧАНИЕ: ItemTooltipCallback (отображение цены при наведении) полностью удален!
        // ДИМА НЕ НАДО ОНО НЕНАДОООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООООО
        // ГАНДОН

        // НО: Я добавил тут Тултип ТОЛЬКО для Мутаций (чтобы было написано "✨ Shiny" на рыбе, как ты просил).
        // Если рыба не зачарована, ничего писаться не будет. Цена тут тоже НЕ пишется.
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            FishMutation mutation = FishMutation.getMutation(stack);
            if (mutation != FishMutation.NONE) {
                lines.add(1, Component.literal(mutation.getDisplayName()).withStyle(mutation.getColor()));
            }
        });
    }

    public static void registerCast(FishingRodItem rod) {
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