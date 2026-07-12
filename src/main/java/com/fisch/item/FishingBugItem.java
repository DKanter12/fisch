package com.fisch.item;

public class FishingBugItem extends Bait {
    public FishingBugItem(Properties properties) {
        // Передаем базовые настройки предмета и силу приманки (например, 15)
        super(properties, 0.02f, 0.02f);
    }
}