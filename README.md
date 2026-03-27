# My2DGame

A 2D Android game built with Java using `SurfaceView` and `Canvas`.

## Features

- Custom game loop with fixed update rate (30 UPS) and uncapped rendering
- Virtual joystick for player movement
- Player character controlled via touch input
- Enemy AI that chases the player
- On-screen FPS/UPS debug overlay

## Project Structure

```
app/src/main/java/com/example/my2dgame/
├── MainActivity.java       # Entry point
├── Game.java               # SurfaceView: handles drawing, input, and game state
├── GameLoop.java           # Fixed-timestep game loop thread
├── Joystick.java           # Virtual joystick input
└── object/
    ├── GameObject.java     # Abstract base class (position, velocity)
    ├── Circle.java         # Abstract circle with draw + screen clamping
    ├── Player.java         # Joystick-controlled player
    └── Enemy.java          # AI enemy that follows the player
```

## Requirements

- Android Studio Ladybug or newer
- Android SDK 35
- Min SDK 28 (Android 9.0)

## Build & Run

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run on an emulator or physical device (API 28+).

## License

[MIT](LICENSE)
