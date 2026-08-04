package com.fisch.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ClientNetwork {

    public static final ResourceLocation MONEY_SYNC_ID = new ResourceLocation("fisch", "money_sync");

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(MONEY_SYNC_ID, (client, handler, buf, responseSender) -> {
            long money = buf.readLong();

            client.execute(() -> {
                CurrencyHud.clientMoney = money;
                ClientMoneyStorage.setBalance(money);
            });
        });
    }
}