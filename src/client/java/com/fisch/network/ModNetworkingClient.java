package com.fisch.network;

import com.fisch.networking.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.FishingRodItem;

public class ModNetworkingClient {

    public static void sendOpenBaitMenu() {

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        if (!(minecraft.player.getMainHandItem().getItem()
                instanceof FishingRodItem)) {
            return;
        }

        FriendlyByteBuf buf =
                PacketByteBufs.create();

        ClientPlayNetworking.send(
                ModPackets.OPEN_BAIT_MENU,
                buf
        );
    }
}