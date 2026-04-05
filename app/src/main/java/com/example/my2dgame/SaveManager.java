package com.example.my2dgame;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages all persistent data for Star Defender, including high scores, 
 * currency (Scrap), permanent hangar upgrades, settings, and milestones.
 */
public class SaveManager {
    private static final String PREF_NAME = "my2dgame_prefs";
    
    // Keys
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_TOTAL_SCRAP = "total_scrap";
    private static final String KEY_SELECTED_SHIP = "selected_ship";
    private static final String KEY_MAX_WAVE = "max_wave_reached";
    private static final String KEY_OWNED_SHIPS = "owned_ships_mask";
    private static final String KEY_BOSSES_DEFEATED = "bosses_defeated_mask";
    
    // Settings Keys
    private static final String KEY_IS_MUTED = "is_muted";
    private static final String KEY_IS_RETRO_MODE = "is_retro_mode";
    private static final String KEY_IS_SHAKE_ENABLED = "is_shake_enabled";
    
    // Upgrade Keys (0-5 levels)
    public static final String UPGRADE_HULL = "upg_hull";
    public static final String UPGRADE_SPEED = "upg_speed";
    public static final String UPGRADE_FIRE = "upg_fire";
    public static final String UPGRADE_MAGNET = "upg_magnet";
    public static final String UPGRADE_BATTERY = "upg_battery";

    private final SharedPreferences prefs;

    public SaveManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Ensure first ship is owned by default (mask bit 0 for TECH)
        if (getOwnedShipsMask() == 0) {
            setShipOwned(0);
        }
    }

    // --- High Score & Waves ---
    public int getHighScore() {
        return prefs.getInt(KEY_HIGH_SCORE, 0);
    }

    public boolean trySaveHighScore(int score) {
        if (score > getHighScore()) {
            prefs.edit().putInt(KEY_HIGH_SCORE, score).apply();
            return true;
        }
        return false;
    }

    public int getMaxWaveReached() {
        return prefs.getInt(KEY_MAX_WAVE, 1);
    }

    public boolean updateMaxWave(int wave) {
        if (wave > getMaxWaveReached()) {
            prefs.edit().putInt(KEY_MAX_WAVE, wave).apply();
            return true;
        }
        return false;
    }

    // --- Currency ---
    public int getTotalScrap() {
        return prefs.getInt(KEY_TOTAL_SCRAP, 0);
    }

    public void addScrap(int amount) {
        int current = getTotalScrap();
        prefs.edit().putInt(KEY_TOTAL_SCRAP, current + amount).apply();
    }

    public boolean spendScrap(int amount) {
        int current = getTotalScrap();
        if (current >= amount) {
            prefs.edit().putInt(KEY_TOTAL_SCRAP, current - amount).apply();
            return true;
        }
        return false;
    }

    // --- Upgrades ---
    public int getUpgradeLevel(String upgradeKey) {
        return prefs.getInt(upgradeKey, 0);
    }

    public void incrementUpgrade(String upgradeKey) {
        int current = getUpgradeLevel(upgradeKey);
        if (current < 5) {
            prefs.edit().putInt(upgradeKey, current + 1).apply();
        }
    }

    // --- Ship Selection & Ownership ---
    public int getSelectedShipIndex() {
        return prefs.getInt(KEY_SELECTED_SHIP, 0);
    }

    public void setSelectedShipIndex(int index) {
        prefs.edit().putInt(KEY_SELECTED_SHIP, index).apply();
    }

    public int getOwnedShipsMask() {
        return prefs.getInt(KEY_OWNED_SHIPS, 0);
    }

    public boolean isShipOwned(int shipIndex) {
        return (getOwnedShipsMask() & (1 << shipIndex)) != 0;
    }

    public void setShipOwned(int shipIndex) {
        int mask = getOwnedShipsMask();
        mask |= (1 << shipIndex);
        prefs.edit().putInt(KEY_OWNED_SHIPS, mask).apply();
    }

    // --- Boss Progress ---
    public int getBossesDefeatedMask() {
        return prefs.getInt(KEY_BOSSES_DEFEATED, 0);
    }

    public boolean isBossDefeated(int bossIndex) {
        return (getBossesDefeatedMask() & (1 << bossIndex)) != 0;
    }

    public void setBossDefeated(int bossIndex) {
        int mask = getBossesDefeatedMask();
        mask |= (1 << bossIndex);
        prefs.edit().putInt(KEY_BOSSES_DEFEATED, mask).apply();
    }

    // --- Settings ---
    public boolean isMuted() {
        return prefs.getBoolean(KEY_IS_MUTED, false);
    }

    public void setMuted(boolean muted) {
        prefs.edit().putBoolean(KEY_IS_MUTED, muted).apply();
    }

    public boolean isRetroMode() {
        return prefs.getBoolean(KEY_IS_RETRO_MODE, false);
    }

    public void setRetroMode(boolean retroMode) {
        prefs.edit().putBoolean(KEY_IS_RETRO_MODE, retroMode).apply();
    }

    public boolean isShakeEnabled() {
        return prefs.getBoolean(KEY_IS_SHAKE_ENABLED, true);
    }

    public void setShakeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_IS_SHAKE_ENABLED, enabled).apply();
    }

    public void clearAllData() {
        prefs.edit().clear().apply();
        // Set defaults
        setShipOwned(0);
        setShakeEnabled(true);
    }
}
