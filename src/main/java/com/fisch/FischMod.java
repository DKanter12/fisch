package com.fisch;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FischMod implements ModInitializer {
	public static final String MODID = "fisch";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final ResourceLocation FISH_GUI_PACKET_ID = new ResourceLocation("fisch", "open_fish_gui");
    @Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

	}
}