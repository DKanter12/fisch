package com.fisch.client;

import com.fisch.FischMod;
import com.fisch.client.hud.CoinHudOverlay;
import com.fisch.client.model.FishMerchantModel;
import com.fisch.client.model.FishMongerModel;
import com.fisch.client.model.FishMongerNetherModel;
import com.fisch.client.model.FishermanWizardModel;
import com.fisch.client.renderer.CustomVillagerRenderer;
import com.fisch.client.renderer.FishMongerNetherRenderer;
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
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.KeyMapping;
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
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import static com.fisch.FischMod.MODID;

public class FischModClient implements ClientModInitializer {

    public static final ResourceLocation FINISH_MINIGAME_PACKET =
            new ResourceLocation(MODID, "finish_minigame");

    // Создаем бинд клавиши для скрытия баланса
    public static KeyMapping toggleCoinKey;

    @Override
    public void onInitializeClient() {
        registerCast(ModItems.ICE_ROD);
        registerCast(ModItems.SAND_ROD);
        registerCast(ModItems.JUNGLE_ROD);
        registerCast(ModItems.SWAMP_ROD);
        registerCast(ModItems.MUSHROOM_ROD);

        // --- РЕГИСТРАЦИЯ ЭКРАНОВ (SCREENS) ---
        MenuScreens.register(ModScreenHandlers.BAIT_MENU, BaitScreen::new);
        MenuScreens.register(ModScreenHandlers.ENCHANTMENT_ALTAR_MENU, EnchantmentAltarScreen::new);

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
                                    new FishCatchScreen(fishName, fishRarity, control, resilience)
                            )
                    );
                }
        );

        // Включаем обработчик пакетов из ClientNetwork
        ClientNetwork.registerReceivers();

        // Регистрация HUD
        HudRenderCallback.EVENT.register(new CoinHudOverlay());

        // --- РЕГИСТРАЦИЯ ГОРЯЧЕЙ КЛАВИШИ ---
        toggleCoinKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Скрыть/Показать баланс", // Имя в настройках управления
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V, // Клавиша по умолчанию (V)
                "Fisch Mod" // Категория в настройках управления
        ));

        // Отслеживаем нажатие клавиши каждый тик
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleCoinKey.consumeClick()) {
                // Инвертируем состояние (скрываем/показываем)
                CoinHudOverlay.isHidden = !CoinHudOverlay.isHidden;
            }
        });

        // --- СВЯЗЬ GUI С MENU TYPES ---
        MenuScreens.register(ModMenuTypes.FISH_MERCHANT_MENU, FishMerchantScreen::new);
        MenuScreens.register(ModMenuTypes.FISH_MONGER_MENU, FishMongerScreen::new);
        MenuScreens.register(ModMenuTypes.WIZARD_MENU, WizardScreen::new);

        // --- РЕНДЕР И МОДЕЛИ (MODELS & RENDERERS) ---
        EntityModelLayerRegistry.registerModelLayer(FishMerchantModel.LAYER_LOCATION, FishMerchantModel::createBodyLayer);
        EntityRendererRegistry.register(EntityType.VILLAGER, CustomVillagerRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FishMerchantClothesModel.LAYER_LOCATION, FishMerchantClothesModel::createBodyLayer);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityType.VILLAGER && entityRenderer instanceof VillagerRenderer villagerRenderer) {
                registrationHelper.register(new FishMerchantLayer(villagerRenderer, context.getModelSet()));
            }
        });

        // Обычный Fish Monger
        EntityRendererRegistry.register(ModEntities.FISH_MONGER, FishMongerRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FishMongerModel.LAYER_LOCATION, FishMongerModel::createBodyLayer);

        // Адский Fish Monger
        EntityRendererRegistry.register(ModEntities.FISH_MONGER_NETHER, FishMongerNetherRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FishMongerNetherModel.LAYER_LOCATION, FishMongerNetherModel::createBodyLayer);

        // Волшебник
        EntityRendererRegistry.register(ModEntities.FISHERMAN_WIZARD, FishermanWizardRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FishermanWizardModel.LAYER_LOCATION, FishermanWizardModel::createBodyLayer);

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
                    if (entity == null) return 0.0F;

                    boolean mainHand = entity.getMainHandItem() == stack;
                    boolean offHand = entity.getOffhandItem() == stack;

                    if (entity.getMainHandItem().getItem() instanceof FishingRodItem) {
                        offHand = false;
                    }

                    return (mainHand || offHand) && entity instanceof Player player && player.fishing != null
                            ? 1.0F : 0.0F;
                }
        );
    }
}