# Star Defender - Android Top-Down Rogue-lite Engine

A high-performance 2D top-down shooter engine for Android, implemented in Java using low-level `SurfaceView` and `Canvas` rendering. This project demonstrates professional mobile game architecture, including zero-allocation loops, advanced visual feedback, and a deep Rogue-lite meta-progression system.

## 🚀 Professional Game Features

### 1. Rogue-lite Meta-Progression (Project Hangar)
- **Star Scrap Economy**: A persistent currency collected from enemies and awarded via mission stipends.
- **The Hangar**: A persistent upgrade shop where players can permanently boost:
    - **Hull Integrity**: Starting HP.
    - **Fusion Thrusters**: Movement speed.
    - **Weapon Overclock**: Firing rate.
    - **Scrap Magnet**: Currency collection radius.
    - **Emergency Battery**: Starting and recharging shields.
- **The Shipyard**: Choose between specialized hulls:
    - **The Tech**: Balanced stats with doubled power-up duration.
    - **The Ghost**: Fast, low-HP ship with Piercing Projectiles (Unlockable).
    - **The Aegis**: Slow, high-HP tank with Double-Shot projectiles (Unlockable).
- **Milestone Gating**: Progressive content unlocking based on all-time max wave and boss defeats.

### 2. High-Performance Graphics & UI
- **Responsive UI**: Fully resolution-independent HUD and menus using dynamic scaling and percentage-based layouts.
- **Immersive Mode**: Fullscreen "Sticky Immersive" integration for 100% pixel utilization.
- **Sprite Engine**: WebP sprites with procedural background removal and `PorterDuff` color tinting.
- **Parallax Background**: 3-layer scrolling starfield with atmospheric nebulas.
- **Directional Rendering**: Entities rotate to face movement vectors, including engine trails for ships and rockets.

### 3. "Game Juice" & Visual Polish
- **Advanced Feedback**: 
  - **Floating Status Text**: Real-time damage and pickup labels.
  - **Smooth Flash Effect**: Entities flash white upon taking damage.
  - **Dynamic Screen Shake**: Multi-layered intensities based on impact severity.
  - **Low-HP Vignette**: Pulsing red screen-edge danger warning.
  - **Score "Pop"**: HUD animation for significant score events.

### 4. Modular Engine Architecture
- **Component Managers**: Extracted logic into specialized controllers (`EnemyManager`, `ProjectileManager`, `ScrapManager`, `ParticleManager`, `UIManager`).
- **Technical Standards**: Strict adherence to **Zero-Allocation** loops and **Bitmap Caching** to ensure 60FPS performance.

## 🛠 Technical Standards (`AGENTS.md`)
- **Memory Management**: Mandatory object pooling for all ephemeral entities.
- **Modularity**: Subsystems decoupled to maintain readability and scalability.
- **Persistence**: Centralized `SaveManager` for all `SharedPreferences` operations.

## Technical Specifications
- **Render Engine**: Android `Canvas` API via `SurfaceHolder`.
- **Language**: Java 17.
- **Minimum SDK**: API 28 (Android 9.0).
- **Target SDK**: API 35.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
