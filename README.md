# TripWise

A new, from-scratch travel planner: Android (Kotlin, Jetpack Compose, Material 3) and a Node.js/Express backend that holds the Gemini API key and other server-side integrations.

> Status: **foundation only**. No product features yet.

## Layout

```
tripwise/
├── .github/workflows/ci.yml  Android build + tests; backend typecheck + tests
├── android/                  Gradle project (open this folder in Android Studio)
│   ├── build-logic/          Convention plugins: tripwise.android.{application,library,compose,feature,room}, tripwise.hilt, tripwise.jvm.library
│   ├── gradle/libs.versions.toml   All dependency versions
│   ├── compose_stability.conf      Classes Compose should treat as stable
│   ├── app/                  Shell: Application, MainActivity, NavHost, bottom bar, AppModule (NetworkConfig)
│   ├── core/
│   │   ├── common/     (JVM) Outcome, AppError, dispatchers/scopes, NetworkMonitor contract
│   │   ├── model/      (JVM) Domain models
│   │   ├── domain/     (JVM) Repository interfaces (+ use cases when needed)
│   │   ├── network/    (JVM) Retrofit API, DTOs, ApiErrorMapper, network data source
│   │   ├── testing/    (JVM) Fakes + MainDispatcherRule (testImplementation only)
│   │   ├── data/             Repository implementations, mappers, ConnectivityManager monitor
│   │   ├── database/         Room setup + converters (database class arrives with the first entity)
│   │   ├── datastore/        Preferences DataStore
│   │   ├── designsystem/     TripWiseTheme tokens + Tw* components
│   │   └── ui/               State views (loading/empty/error/offline), AppError → message mapping
│   └── feature/
│       ├── home/             Reference implementation of Route → ViewModel → UiState → Screen
│       └── explore/ trips/ assistant/ profile/   Placeholder tabs (navigation shell only)
└── backend/                  Node 22, Express 5, TypeScript, zod, pino (+ @google/genai, unused yet)
```

Dependencies point one way: `app → feature:* → core:ui → core:designsystem`, and `feature:* → core:domain ← core:data → core:network / database / datastore`.
Features never depend on other features. `:app` wires cross-feature navigation.

## Conventions

- **Screens:** `XxxScreen(viewModel)` (stateful) → `XxxScreen(state, onAction)` (stateless; previews and tests target this).
- **ViewModels:** expose one `StateFlow<XxxUiState>` and receive intents through `onAction(XxxAction)`.
- **Repositories:** return `Outcome<T>` or `Flow<T>` and never throw. Errors are `AppError`, mapped to text in `core:ui`.
- **Backend errors:** always `{ "error": { code, message, details?, serverVersion? } }`. Codes are shared with `ApiErrorCode` on Android.
- **Navigation:** each feature exposes a `@Serializable` route, `NavController.navigateToXxx()` and `NavGraphBuilder.xxxSection()`.
- **Secrets:** the Maps key and backend URLs come from `android/local.properties`. The Gemini key is only ever on the backend.

## Android: getting started

1. Copy `android/local.properties.example` to `android/local.properties` and fill it in.
2. Open `android/` in Android Studio and sync.
3. Run `:app`. Debug builds call `http://10.0.2.2:3000/` (the emulator's host). Release builds are HTTPS-only and need `BACKEND_BASE_URL_RELEASE`.

Versions marked `⚠` in `libs.versions.toml` could not be verified when this was set up, because Google Maven was unreachable. If a sync fails on one of them, bump it to the current stable version.

## Backend: getting started

```bash
cd backend
cp .env.example .env
npm install
npm run dev      # http://localhost:3000/api/v1/health
npm test
```
