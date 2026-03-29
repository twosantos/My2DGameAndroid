# Android Game Development Best Practices - Star Defender

This document outlines the architectural and performance standards for AI agents working on the "Star Defender" project.

## 1. Zero-Allocation Game Loop
The most critical rule for smooth gameplay (avoiding Garbage Collection stutters):
- **NEVER** use the `new` keyword inside `update()` or `draw()` methods.
- **Object Pooling**: All frequently created objects (Projectiles, Enemies, Particles, FloatingText) must use a `List` based pool. Re-initialize them using a `reset()` or `init()` method.
- **Reusable Objects**: Use class-level instances for `Rect`, `RectF`, `Path`, and `Matrix` to avoid frame-by-frame allocations.

## 2. Asset & Bitmap Management
- **SpriteCache**: Always use `SpriteCache.getSprite(context, id)` to retrieve Bitmaps.
- **One-Time Processing**: Heavy operations like removing white backgrounds or creating tinted variants must be done once and cached.
- **Preloading**: New assets must be added to the `SpriteCache.preload()` call in the `Game` constructor to prevent "first-spawn" lag.

## 3. Modular Architecture
- **Single Responsibility**: Keep features in dedicated classes (e.g., `ParallaxLayer.java`, `EngineTrail.java`, `HealthBar.java`).
- **File Length**: Avoid giant files. If a `Game.java` logic block grows too large, extract it into a utility or a component class.
- **Packages**: Keep Game Objects in the `com.example.my2dgame.object` package and engine/UI logic in the core package.

## 4. UI/UX & Feedback (Game Juice)
- **Visual Feedback**: Every action should have a reaction.
    - Damage -> White Flash + Screen Shake.
    - Destruction -> Particle Explosion + Floating Score Text.
    - Low Health -> Pulsing Vignette.
- **Dynamic HUD**: Use scaling/color transitions (Score Pop) to reward player progress.
- **Transparency**: Use `PorterDuff.Mode.SRC_ATOP` for sprite tinting to keep texture details while changing colors.

## 5. Mathematical Conventions
- **Rotation**: Standard math (`Math.atan2`) assumes 0 degrees is **Right**.
- **Ship Orientation**: Our assets face **Up** by default, requiring a `+90` degree offset in rotation logic.
- **Rocket Orientation**: Rocket assets face **Bottom-Right**, requiring a `-45` degree offset.
- **Scaling**: Always scale drawing coordinates based on `screenWidth` and `screenHeight` to ensure consistency across different Android devices.

## 6. Rendering Performance
- **Canvas Saves**: Always pair `canvas.save()` with `canvas.restore()` when performing rotations or translations to avoid corrupting the global coordinate state.
- **Draw Order**: Backgrounds -> Parallax Layers -> Trails -> Entities -> Particles -> HUD.
- **Anti-Aliasing**: Enable `paint.setAntiAlias(true)` for UI elements, but monitor performance if used on hundreds of particles.
