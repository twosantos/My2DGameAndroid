# Star Defender - Android Top-Down Rogue-lite Engine

A high-performance 2D top-down shooter engine for Android, implemented in Java using low-level `SurfaceView` and `Canvas` rendering. This project demonstrates professional mobile game architecture, including zero-allocation loops, advanced visual feedback, and modular entity management.

## 🚀 Professional Game Features

### 1. High-Performance Graphics
- **Sprite Engine**: Transitioned from primitive shapes to high-quality WebP sprites with procedural background removal and `PorterDuff` color tinting.
- **Parallax Background**: 3-layer scrolling starfield with procedural diamond-shaped stars and atmospheric, slow-drifting nebulas for deep-space immersion.
- **SpriteCache**: A centralized asset manager that preloads and caches processed Bitmaps to ensure zero-lag entity spawning.
- **Directional Rendering**: Entities dynamically rotate to face their movement vector, including specialized random tumbling for asteroids.

### 2. "Game Juice" & Visual Polish
- **Advanced Feedback**: 
  - **Floating Status Text**: Real-time damage and pickup labels that rise and fade.
  - **Smooth Flash Effect**: Entities flash white upon taking damage for instant hit-confirmation.
  - **Dynamic Screen Shake**: Layered shake intensities (Light impact, Medium kill, Heavy Boss/Damage) with linear decay.
  - **Low-HP Vignette**: Pulsing red screen-edge effect when health is critical (<= 2 HP).
  - **Score "Pop"**: HUD text scales and changes color briefly on significant score increases.

### 3. Modular Rogue-lite Architecture
- **Component Managers**: Extracted logic into dedicated controllers:
  - `EnemyManager`: Handles spawning, radial splitting logic, and AI.
  - `ProjectileManager`: Manages firing, pooling, and advanced Homing Missile steering.
  - `ParticleManager`: Controls explosion effects and memory pooling.
  - `UIManager`: Orchestrates menus, HUD, and power-up timers.
- **Wave System**: Structured wave progression with a Main Menu **Wave Selector** (Waves 1-20) and cinematic **Start-up/Fly-in animations**.

### 4. Advanced Gameplay Mechanics
- **Homing Missiles**: Specialized pickup that transforms rockets into target-seeking missiles that actively track the nearest asteroid.
- **The "Splitter"**: Large green asteroids that fragment into 3 tiny, high-speed scraps upon destruction.
- **Engine Trails**: Particle-based ship exhaust that follows the ship's rotation and movement speed.
- **Power-up Timers**: Visual countdown bars in the HUD for active status effects (Shield, Speed, Rapid Fire, Homing).

## 🛠 Technical Standards (`AGENTS.md`)
This project follows strict performance and architectural rules:
- **Zero-Allocation Loop**: Mandatory object pooling and reuse of `Rect`/`Path` objects inside `update()`/`draw()` to avoid GC stutters.
- **Modular Refactor**: Decoupled engine subsystems to maintain small, readable file sizes.
- **Preloading**: Mandatory asset pre-processing in `SpriteCache` during game initialization.

## Technical Specifications
- **Render Engine**: Android `Canvas` API via `SurfaceHolder`.
- **Language**: Java 17.
- **Minimum SDK**: API 28 (Android 9.0).
- **Architecture Pattern**: Manager-based modular design.

## Getting Started
1. Clone the repository.
2. Open in Android Studio.
3. Consult `AGENTS.md` for development standards.
4. Consult `HISTORY.md` for technical context on existing features.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
