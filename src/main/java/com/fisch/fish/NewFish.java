package com.fisch.fish;

public class NewFish {
    public String name;
    public int rarity;
    long minWeight;
    long maxWeight;
    public String bestBait;
    public String bestWeather;
    public String bestTime;

    public NewFish(String name,int rarity, long minWeight, long maxWeight, String bestBait, String bestWeather, String bestTime) {
        this.name = name;
        this.rarity = rarity;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.bestBait = bestBait;
        this.bestWeather = bestWeather;
        this.bestTime = bestTime;
    }
}
