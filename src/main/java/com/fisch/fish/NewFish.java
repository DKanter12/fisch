    package com.fisch.fish;

    import net.minecraft.world.item.Item;

    public class NewFish extends Item {
        public String name;
        public int rarity;
        long minWeight;
        long maxWeight;
        public String bestBait;
        public String bestWeather;
        public String bestTime;


        public NewFish(Properties properties,String name,int rarity, long minWeight, long maxWeight, String bestBait, String bestWeather, String bestTime) {
            super(properties);
            this.name = name;
            this.rarity = rarity;
            this.minWeight = minWeight;
            this.maxWeight = maxWeight;
            this.bestBait = bestBait;
            this.bestWeather = bestWeather;
            this.bestTime = bestTime;
        }
    }
