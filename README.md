# TripWise

A new, from-scratch travel planner: Android (Kotlin, Jetpack Compose, Material 3) and a Node.js/Express backend that holds the Gemini API key and other server-side integrations.

> Status: **foundation only**. No product features yet.

## Layout

```
tripwise/
├── android/                  Gradle project (open this folder in Android Studio)
│   ├── build-logic/          Shared build (convention) plugins: tripwise.android.*, tripwise.jvm.library
│   ├── gradle/libs.versions.toml   All dependency versions
│   ├── app/                  Entry point: Application, MainActivity, NavHost, app shell
│   └── core/
│       ├── model/            Pure Kotlin models
│       ├── domain/           Use cases + repository interfaces (pure Kotlin)
│       ├── data/             Repository implementations, DataStore (Room added with first entity)
│       ├── network/          Retrofit/OkHttp/kotlinx.serialization client for the TripWise backend
│       ├── designsystem/     TripWiseTheme (color, type, shape, spacing) + reusable components
│       └── ui/               Shared screen-level UI helpers
└── backend/                  Node 22, Express 5, TypeScript, zod, @google/genai
```

Dependency direction: `app → feature:* → core:ui/designsystem, core:domain ← core:data → core:network → core:model`.
Feature modules (`:feature:*`) are added as features are built.

## Architecture

- MVVM + one-way data flow: each screen has a ViewModel exposing an immutable `StateFlow<UiState>`.
- Hilt for DI, KSP for annotation processing.
- Type-safe Navigation Compose (`@Serializable` routes).
- AGP 9 built-in Kotlin (no `kotlin-android` plugin).
- Secrets: the Maps/Places key is read from `android/local.properties` and injected into the manifest. **The Gemini key is only on the backend.**

## Android: getting started

1. Copy `android/local.properties.example` to `android/local.properties` and fill it in.
2. Open `android/` in Android Studio and sync.
3. Run `:app`. Debug builds may call `http://10.0.2.2:3000/` (the emulator's host). Release builds are HTTPS-only.

Versions marked `⚠` in `libs.versions.toml` could not be verified when this was set up, because Google Maven was unreachable. If a sync fails on one of them, bump it to the current stable version.

## Backend: getting started

```bash
cd backend
cp .env.example .env
npm install
npm run dev      # http://localhost:3000/api/health
npm test
```
