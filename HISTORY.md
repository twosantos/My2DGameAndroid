# Star Defender - Feature History & Technical Context

This document maintains a chronological history of all core features and technical optimizations implemented in the Star Defender project.

| Feature / Change | Description | Technical Implementation |
| :--- | :--- | :--- |
| **Sprite Engine** | Transitioned from primitive shapes to WebP sprites. | Implemented custom background removal and `PorterDuff` color tinting. |
| **SpriteCache** | Optimized asset loading. | Replaced per-spawn decoding with a centralized `Map<Integer, Bitmap>` cache. |
| **Parallax Background** | Multi-layer space environment. | 3-layer scrolling system with procedural diamond stars and atmospheric nebulas. |
| **Directional Rotation** | Entities face movement direction. | Used `Math.atan2` with asset-specific offsets (+90 for ship, -45 for rocket). |
| **Engine Trails** | Visual ship exhaust. | Particle emitter attached to the back of the player ship. |
| **Splitter Enemy** | Specialized asteroid behavior. | Large green asteroid that spawns 3 small, fast ones radially upon destruction. |
| **Refactor (Modular)** | Decoupled core engine logic. | Extracted logic into `UIManager`, `EnemyManager`, `ProjectileManager`, `ParticleManager`, and `ScrapManager`. |
| **Homing Missiles** | Advanced weapon pickup. | Projectiles that scan for and track the nearest enemy using a steering vector. |
| **Modern UI** | Centered HUD and menus. | Implemented `HealthBar.java` and moved HUD/Menu logic to `UIManager`. |
| **Advanced Feedback** | "Game Juice" package. | Floating status text, animated wave headers, score "pop" scaling, and low-HP vignette. |
| **Startup Animation** | Cinematic wave entry. | Added `startFlyIn` state to Player for automated entry and wave transitions. |
| **Wave Selector** | Level progression control. | Main Menu UI allowing players to start between Wave 1 and 20. |
| **Power-up Timers** | Temporal HUD feedback. | Visual countdown bars for active status effects (Speed, Rapid Fire, Shield, Homing). |
| **SaveManager** | Centralized persistence layer. | Replaced fragmented `SharedPreferences` logic with a unified manager for high scores, scrap, and upgrades. |
| **Scrap Currency** | In-game economy. | Implemented `Scrap.java` with magnetic pull logic and `ScrapManager` for drop-rate control. |
| **Shipyard System** | Multiple specialized hulls. | Implemented `ShipProfile` enum to define base stats and unique ship traits (Aegis: Double-Shot, Ghost: Piercing, Tech: Long Power-ups). |
| **Hangar Menu** | Permanent meta-progression. | Added a persistent upgrade shop for HP, Speed, Firing Rate, Magnet, and Battery levels. |
| **Milestone Unlocks** | Achievement-gated content. | Implemented locking logic for ships and weapons based on waves reached and bosses defeated. |
| **Responsive UI** | Resolution-independent scaling. | Overhauled `UIManager` to use dynamic scaling factors and percentage-based coordinates for all screen types. |
| **Sticky Immersive** | Fullscreen display integration. | Added `SYSTEM_UI_FLAG_IMMERSIVE_STICKY` to maximize pixel usage and hide navigation bars. |
