package com.fisch.client.screen;

import com.fisch.FischMod;
import com.fisch.command.ModCommands;
import com.fisch.fish.FishMutation;
import com.fisch.menu.WizardMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class WizardScreen extends AbstractContainerScreen<WizardMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    private Button enchantButton;

    public WizardScreen(WizardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 130;
        int buttonHeight = 20;
        int x = this.leftPos + (this.imageWidth - buttonWidth) / 2;
        int y = this.topPos + 44;

        this.enchantButton = Button.builder(
                Component.translatable("gui.fisch.enchant"),
                button -> {
                    ClientPlayNetworking.send(new ResourceLocation(FischMod.MODID, "enchant_fish"), PacketByteBufs.create());
                }
        ).bounds(x, y, buttonWidth, buttonHeight).build();

        this.addRenderableWidget(this.enchantButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack stack = this.menu.getSlot(0).getItem();

        if (stack.isEmpty() || !ModCommands.FISH_PRICES.containsKey(stack.getItem())) {
            this.enchantButton.setMessage(Component.translatable("gui.fisch.enchant"));
            this.enchantButton.active = false;
        } else {
            long enchantCost = FishMutation.getEnchantCost(stack);
            this.enchantButton.setMessage(Component.translatable("gui.fisch.enchant_cost", enchantCost));
            this.enchantButton.active = true;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, 71);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos + 71, 0, 126, this.imageWidth, 96);
        guiGraphics.fill(this.leftPos + 7, this.topPos + 17, this.leftPos + 169, this.topPos + 71, 0xFFC6C6C6);

        int slotX = this.leftPos + 79;
        int slotY = this.topPos + 17;
        guiGraphics.blit(TEXTURE, slotX, slotY, 7, 17, 18, 18);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}