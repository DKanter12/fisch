package com.fisch.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ClientNetwork {

    // Айдишник пакета синхронизации денег
    public static final ResourceLocation MONEY_SYNC_ID = new ResourceLocation("fisch", "money_sync");

    public static void registerReceivers() {
        // Регистрируем глобальный прослушиватель пакета на клиенте
        ClientPlayNetworking.registerGlobalReceiver(MONEY_SYNC_ID, (client, handler, buf, responseSender) -> {
            // Читаем число (long), которое нам прислал сервер
            long money = buf.readLong();

            // Выполняем обновление в основном потоке Майнкрафта, чтобы не было микрофризов
            client.execute(() -> {
                // Записываем баланс ТОЧНО в твою переменную в CurrencyHud
                CurrencyHud.clientMoney = money;
            });
        });
    }
}