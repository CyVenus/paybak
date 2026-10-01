# Paybak

One repo for all Paybak apps.

The mobile apps are built from the design spec in [`docs/design/`](docs/design/README.md): screens, components, tokens, the domain model and a demo dataset that reproduces the Figma numbers.

| Folder     | Platform | Stack                   | Status      |
| ---------- | -------- | ----------------------- | ----------- |
| `web/`     | Web      | React 18 + Vite, pnpm   | In progress |
| `android/` | Android  | Kotlin, Jetpack Compose | All screens built (local data, no backend) |
| `ios/`     | iOS      | Swift, SwiftUI          | All screens built (local data, no backend) |

## Getting started

### Web

```sh
cd web
pnpm install
pnpm dev
```

### Android

Open `android/` in Android Studio, or build from the terminal:

```sh
cd android
./gradlew assembleDebug
```

### iOS

```sh
open ios/paybak/paybak.xcodeproj
```
