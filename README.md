# My2DGame - Android Top-Down Engine

A high-performance 2D top-down shooter engine for Android, implemented in Java using low-level `SurfaceView` and `Canvas` rendering. This project demonstrates advanced mobile game architecture, including decoupled update/render loops, entity management, and custom input systems.

## Core Technical Features

### 1. Robust Game Architecture
- **Decoupled Game Loop**: Implements a fixed-step update loop (30 UPS) decoupled from the rendering frame rate, ensuring consistent physics and logic regardless of hardware performance.
- **State Machine**: Centralized game state management (`MENU`, `PLAYING`, `PAUSED`, `GAME_OVER`) to handle complex UI transitions and logic branching.
- **Object Pooling**: Highly optimized memory management using object pools for `Enemies`, `Projectiles`, and `Particles` to minimize Garbage Collector (GC) overhead during high-intensity gameplay.

### 2. Advanced Control System
- **Dynamic Virtual Joysticks**: Custom-built input system featuring dynamic re-centering. Joysticks initialize at the user's first touch point within their respective screen halves, maximizing ergonomic comfort and tactile precision.

### 3. Entity & Combat Systems
- **Wave Management**: A structured wave system with difficulty scaling, cooldown periods, and specialized "Boss Waves" every 5 levels.
- **Entity AI**: Multiple specialized enemy behaviors (Pathing, Fast-tracking, Tanks, and Sinusoidal "ZigZag" movement).
- **Boss Mechanics**: Scaled entities with independent health management and dedicated UI components.
- **Power-up Logic**: Temporal status effects (Speed Boost, Rapid Fire, Shield) and instant restorative items, managed through a modular pickup system.

### 4. Audio & Visual Feedback
- **Sound Management**: Dual-track audio system using `SoundPool` for low-latency sound effects and `MediaPlayer` for background music.
- **VFX & Juice**: 
  - Particle systems for entity destruction.
  - Frame-based screen shake with intensity decay.
  - Canvas-level translation and alpha-blending for damage feedback.

### 5. Mobile Lifecycle Integration
- **Activity Lifecycle Handling**: Automated game state suspension (`PAUSE`) on Activity interruption.
- **Persistence**: High-score data persistence using `SharedPreferences`.
- **Resource Management**: Efficient cleanup of Surface resources and hardware-accelerated drawing.

## Technical Specifications
- **Render Engine**: Android `Canvas` API via `SurfaceHolder`.
- **Language**: Java 8+.
- **Minimum SDK**: API 28 (Android 9.0).
- **Target SDK**: API 35.
- **Architecture Pattern**: Component-based entity design within a centralized game controller.

## Getting Started

### Prerequisites
- Android Studio Ladybug (or newer).
- Android SDK 35.

### Installation
1. Clone the repository.
2. Open in Android Studio.
3. Allow Gradle to sync dependencies.
4. Deploy to a physical device or emulator supporting OpenGL/Hardware Acceleration.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
