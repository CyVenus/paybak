# Paybak

One repo for all Paybak apps.

| Folder     | Platform | Stack                   | Status      |
| ---------- | -------- | ----------------------- | ----------- |
| `web/`     | Web      | React 18 + Vite, pnpm   | In progress |
| `android/` | Android  | Kotlin, Jetpack Compose | Not started |
| `ios/`     | iOS      | Swift, SwiftUI          | Not started |

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
