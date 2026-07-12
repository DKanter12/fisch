package com.fisch.item; // Не забудь указать свой пакет мода

import net.minecraft.world.item.Item;

public class BugNetItem extends Item {

    public BugNetItem(Properties properties) {
        super(properties);
    }

    // В будущем сюда можно будет добавить метод use(),
    // чтобы при клике правой кнопкой мыши сачок ловил жуков в воздухе!
}