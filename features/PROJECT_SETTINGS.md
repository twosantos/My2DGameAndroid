# Project Settings - Player Customization & Optimization

This document outlines the roadmap for adding a Settings menu to "Star Defender," allowing for audio control, visual performance modes, and accessibility options.

## 🚀 Concept Overview
The Settings menu provides players with control over their experience. This includes audio management, a "Retro Mode" for performance/style, and gameplay tweaks.

---

## ⚙️ 1. Core Settings Ideas

### **Audio Controls**
- **Master Mute**: Toggle all game sounds and music on/off.
- **Music Volume**: Slider or toggle specifically for background music.
- **SFX Volume**: Slider or toggle for hits, explosions, and UI sounds.

### **Visual Modes**
- **Retro Mode (Performance)**: 
    - Disables all WebP sprites.
    - Entities are rendered as their base primitive shapes (Circles) with their defined colors.
    - Reduces CPU/GPU load and provides a classic arcade aesthetic.
- **Screen Shake Toggle**: Allow players to disable the camera shake effect (Accessibility).
- **Floating Text Toggle**: Hide "+100" or "SCRAP" labels to reduce visual clutter.

### **Gameplay & Ergonomics**
- **Joystick Static/Dynamic**: Toggle between joysticks that spawn where you touch vs. fixed positions.
- **Sensitivity**: Adjust how fast the ship reacts to joystick input.

### **Data Management**
- **Clear Data**: Permanent reset of Scrap, Upgrades, and High Scores (with a confirmation prompt).

---

## 📝 Implementation Task List

### Phase 1: Persistence & Backend (DONE)
- [x] **Task 1.1**: Update `SaveManager.java` to store booleans for `isMuted`, `isRetroMode`, `isShakeEnabled`, etc.
- [x] **Task 1.2**: Update `SoundManager.java` to check `saveManager.isMuted()` before playing any sound or starting music.
- [x] **Task 1.3**: Update rendering logic in `Enemy`, `Player`, and `Projectile` to skip drawing bitmaps if `isRetroMode` is active.

### Phase 2: Settings UI (DONE)
- [x] **Task 2.1**: Add `SETTINGS` state to `GameState.java`.
- [x] **Task 2.2**: Implement `drawSettings()` in `UIManager.java` with responsive toggles/buttons.
- [x] **Task 2.3**: Add a "SETTINGS" button to the Main Menu.

### Phase 3: Interaction & Logic (DONE)
- [x] **Task 3.1**: Implement `handleSettingsTouch()` in `Game.java`.
- [x] **Task 3.2**: Ensure visual changes (like Retro Mode) apply instantly without restarting the game.
- [x] **Task 3.3**: Implement the "Clear Data" confirmation flow.

---

## 🤖 Agent Instructions (per AGENTS.md)
- Ensure all new UI elements are scaled using the `UIManager` scaling factor.
- Do not allocate new `Paint` or `Rect` objects inside `drawSettings()`.
- Record all setting keys in `SaveManager` to avoid string-key collisions.
