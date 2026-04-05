# Project Hangar - Meta-Progression Strategic Plan

This document outlines the roadmap for transforming "Star Defender" into a Rogue-lite experience through permanent upgrades, ship classes, and milestones.

## 🚀 Concept Overview
"Project Hangar" introduces persistent progression. Players collect "Star Scrap" during runs to purchase permanent ship improvements, ensuring that even a failed run contributes to overall power growth.

---

## 💎 1. Currency: "Star Scrap" (Rebalanced)
A persistent currency used for all meta-upgrades. Balancing is tuned to prevent rapid maxing of stats.

- **Acquisition during Gameplay**:
    - **Normal Asteroids**: 2% drop chance. Value: 1 Scrap.
    - **Specialized Enemies (Splitter/ZigZag)**: 10% drop chance. Value: 1 Scrap.
    - **Bosses**: Guaranteed drop. Value: 15 Scrap.
- **End-of-Run Bonus (Mission Stipend)**:
    - Formula: `Total Waves Cleared * 5`.
- **Scrap Behavior**: 
    - Scrap icons slowly drift toward the bottom of the screen.
    - **Visual Warning**: Icons blink for 2 seconds before vanishing (total life: 8s).
    - **Magnetism**: If within the player's collection radius, Scrap accelerates toward the ship.

## 🛠️ 2. The Hangar (Upgrade Shop)
A new persistent menu for stat increases. Costs increase exponentially: `50, 150, 450, 1200, 3000`.

- **Upgrade Paths (Max 5 Levels each)**:
    - **Reinforced Hull**: +1 Max HP per level (Bonus added to Ship Base HP).
    - **Fusion Thrusters**: +5% Speed per level.
    - **Weapon Overclock**: -5% Firing Interval per level.
    - **Scrap Magnet**: Increases collection radius for Scrap/Pickups (+20% per level).
    - **Emergency Battery**: Level 1-4: Start runs with a temporary Shield. Level 5: Shield recharges every 60s if lost.

## 🛸 3. Specialized Ship Classes (The Shipyard)
Players choose a "Hull Type" in the Hangar. This uses a `ShipProfile` configuration system.

### **Initial Fleet & Gating**
- **The Tech (Engineer)**: 5 HP, 1.0x Speed. *Trait*: Power-up duration is doubled. (UNLOCKED BY DEFAULT)
- **The Ghost (Assassin)**: 3 HP, 1.3x Speed. *Trait*: Projectiles are Piercing. (REACH WAVE 10 TO UNLOCK | COST: 500 SCRAP)
- **The Aegis (Tank)**: 8 HP, 0.8x Speed. *Trait*: Double-Shot. (REACH WAVE 20 TO UNLOCK | COST: 1500 SCRAP)

## 🏆 4. Milestone Unlocks
- **Wave 10**: Unlock "The Ghost" ship for purchase.
- **Wave 20**: Unlock "The Aegis" ship for purchase.
- **Boss 1 Defeated**: Unlock "Homing Missile" drops in all runs.
- *Note: "Splitter" asteroids are available from the beginning to maintain difficulty variety.*

---

## 📝 Implementation Task List

### Phase 1: Persistence & Economy (DONE)
- [x] **Task 1.1**: Create `SaveManager.java`.
- [x] **Task 1.2**: Create `Scrap.java`.
- [x] **Task 1.3**: Update `EnemyManager.java` to drop Scrap.
- [x] **Task 1.4**: Implement collection radius logic in `Player.java`.

### Phase 2: Player & Stat Dynamicism (DONE)
- [x] **Task 2.1**: Refactor `Player.java` to apply `SaveManager` bonuses.
- [x] **Task 2.2**: Implement `ShipProfile` enum with specialized assets.
- [x] **Task 2.3**: Update `Game.java` to award the "Mission Stipend" on `GAME_OVER`.

### Phase 3: The Hangar UI (DONE)
- [x] **Task 3.1**: Add `HANGAR` state.
- [x] **Task 3.2**: Implement `drawHangar()` in `UIManager.java`.
- [x] **Task 3.3**: Implement responsive touch logic for UI.

### Phase 4: Milestone Unlocks & Gating (DONE)
- [x] **Task 4.1**: **Persistence Layer Expansion**
    - Expand `SaveManager.java` to track `max_wave_reached`.
    - Add `owned_ships_mask` (int bitmask) to track which ships have been purchased.
    - Add `bosses_defeated_mask`.
- [x] **Task 4.2**: **Ship Profile Definitions**
    - Update `ShipProfile.java` with `unlockWave` and `scrapPrice` fields.
    - Implement `isUnlocked(saveManager)` and `isOwned(saveManager)` logic.
- [x] **Task 4.3**: **Menu Gating & UI**
    - Update `UIManager.drawMenu()` to render "LOCKED" overlay for ships not yet owned.
    - Add price tags and "BUY" logic to the Ship Selector in the main menu.
    - Cap the **Wave Selector** arrows so the player can't select a wave higher than `max_wave_reached`.
    - Bugfix dynamic sizes of buttons for all kind of screens. Buttons like back or exit to SO must be clickable by all text.
- [x] **Task 4.4**: **Gameplay Gating**
    - Update `Game.spawnPickup()` to only allow `HOMING` type if `Boss 1` has been defeated.
- [x] **Task 4.5**: **Real-time Milestones**
    - Implement a check in `Game.update()` to detect when a player crosses wave thresholds for the first time.
    - Trigger a "NEW MILESTONE REACHED!" notification using `FloatingText` or a HUD toast.
- [x] **Task 4.6**: **Progress Synchronization**
    - Ensure `max_wave_reached` is correctly updated in `saveManager` at the end of every run.

---

## 🤖 Agent Instructions (per AGENTS.md)
- Follow **Zero-Allocation** rules for Scrap entities (Pooling).
- Use `SaveManager` for **ALL** persistent data.
- Ensure `ShipProfile` sprites are added to `SpriteCache.preload()`.
