package com.example.my2dgame;

/**
 * Defines the base characteristics of different ship classes and their unlock requirements.
 */
public enum ShipProfile {
    TECH("The Tech", 5, 1.0f, R.drawable.engineer, "Engineer: Average stats, long power-ups.", 1, 0),
    GHOST("The Ghost", 3, 1.3f, R.drawable.assasin, "Assassin: Low HP, high speed, Piercing.", 10, 500),
    AEGIS("The Aegis", 8, 0.8f, R.drawable.tank, "Tank: High HP, slow, Double-Shot.", 20, 1500);

    private final String name;
    private final int baseHealth;
    private final float speedMultiplier;
    private final int spriteResId;
    private final String description;
    private final int unlockWave;
    private final int scrapPrice;

    ShipProfile(String name, int baseHealth, float speedMultiplier, int spriteResId, String description, int unlockWave, int scrapPrice) {
        this.name = name;
        this.baseHealth = baseHealth;
        this.speedMultiplier = speedMultiplier;
        this.spriteResId = spriteResId;
        this.description = description;
        this.unlockWave = unlockWave;
        this.scrapPrice = scrapPrice;
    }

    public String getName() { return name; }
    public int getBaseHealth() { return baseHealth; }
    public float getSpeedMultiplier() { return speedMultiplier; }
    public int getSpriteResId() { return spriteResId; }
    public String getDescription() { return description; }
    public int getUnlockWave() { return unlockWave; }
    public int getScrapPrice() { return scrapPrice; }

    public boolean isUnlocked(SaveManager saveManager) {
        return saveManager.getMaxWaveReached() >= unlockWave;
    }

    public boolean isOwned(SaveManager saveManager) {
        return saveManager.isShipOwned(this.ordinal());
    }
}
