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
| **Refactor (Modular)** | Decoupled core engine logic. | Extracted logic into `UIManager`, `EnemyManager`, `ProjectileManager`, and `ParticleManager`. |
| **Homing Missiles** | Advanced weapon pickup. | Projectiles that scan for and track the nearest enemy using a steering vector. |
| **Modern UI** | Centered HUD and menus. | Implemented `HealthBar.java` and moved HUD/Menu logic to `UIManager`. |
| **Advanced Feedback** | "Game Juice" package. | Floating status text, animated wave headers, score "pop" scaling, and low-HP vignette. |
| **Startup Animation** | Cinematic wave entry. | Added `startFlyIn` state to Player for automated entry and wave transitions. |
| **Wave Selector** | Level progression control. | Main Menu UI allowing players to start between Wave 1 and 20. |
| **Power-up Timers** | Temporal HUD feedback. | Visual countdown bars for active status effects (Speed, Rapid Fire, Shield, Homing). |
