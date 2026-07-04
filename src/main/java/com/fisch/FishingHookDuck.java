package com.fisch;

import com.fisch.fish.NewFish;

public interface FishingHookDuck {

    NewFish getCustomCatch();

    void setCustomCatch(NewFish fish);

    void finishMiniGame(boolean success);
}