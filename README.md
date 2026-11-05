# TripWise

A new, from-scratch travel planner: Android (Kotlin, Jetpack Compose, Material 3) and a Node.js/Express backend that holds the Gemini API key and other server-side integrations.

> Status: all 13 planned phases implemented. Every push to `main` is built and tested by CI and walked through on a real Android emulator (see **Emulator tour** below).

## Features

| Area | What it does | Needs |
|---|---|---|
| Home | Greeting, search, featured + popular carousels, upcoming trip, interests, recently saved | Backend |
| Explore | 16 Indian destinations: search by name/state, interest filters, adaptive grid, tablet list–detail | Backend |
| Destination | Photo, facts, highlights, food, things to do, map preview, save/share | Backend (map: `MAPS_API_KEY`) |
| Activities | Curated attractions by category, nearby places from Google | Nearby: `PLACES_API_KEY` on backend |
| Maps | Full map with destination, attractions and nearby pins; directions | `MAPS_API_KEY` in `android/local.properties` |
| AI assistant | Streaming travel chat with trip/destination context, retry/stop | `GEMINI_API_KEY` on backend |
| AI itinerary | Destination, days, pace, budget, interests → day-by-day plan → save to a trip | `GEMINI_API_KEY` (falls back to a curated basic plan) |
| Expenses | Cost estimate by category (currency-converted), planned amounts, expense log, totals | Backend (rates) |
| My Trips | Create, view, delete trips; add/edit/delete itinerary activities; budget | – |
| Saved | Save/remove with undo, sorting, consistent hearts everywhere | – |
| Images | Curated Indian destination photos (Wikimedia Commons with author/licence credit, or Unsplash), each reviewed on a CI contact sheet; gradient fallback | – |
| Account | Email/password sign-up/in, session restore, sign-out, account deletion | Backend |

## API keys

- **Backend** (`backend/.env`, or Cloud Run secrets): `GEMINI_API_KEY` (Google AI Studio), `PLACES_API_KEY` (Google Cloud, *Places API (New)* enabled), `JWT_SECRET`, `DATABASE_URL`.
- **Android** (`android/local.properties`): `MAPS_API_KEY` (Google Cloud, *Maps SDK for Android* enabled, restricted to package `com.tripwise.app` / `.debug` + your signing SHA-1).
- **CI emulator tour**: repository secrets `GEMINI_API_KEY` and `MAPS_API_KEY` (the tour also passes the Maps key to the backend as the Places key, so that key must not be restricted to Android apps only).

Without keys the app still runs; those features show a clear "not set up" state.

## Emulator tour

`.github/workflows/demo.yml` starts the backend, boots an Android 14 emulator, runs `AppTourTest` (a scripted walkthrough) and publishes the screenshots to the `ci-screenshots` branch. Catalog photos are checked by the `catalog-images` CI job, which publishes a labelled contact sheet to the `ci-image-review` branch.

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

## Running the app against your local backend

Signing in needs the backend:

1. Start it (`cd backend && npm install && npm run dev`). Without `DATABASE_URL` it uses an embedded Postgres (PGlite) stored in `backend/.data`.
2. Run the app on an emulator. Debug builds call `http://10.0.2.2:3000/`, which is your machine as seen from the emulator.
3. Physical device: set `BACKEND_BASE_URL` in `android/local.properties` to your machine's LAN address. You'll also need to add that host to `app/src/debug/res/xml/network_security_config.xml`.

## Authentication design

- Email + password, hashed with **Argon2id** (OWASP parameters). Login failures give one generic message, and unknown emails take the same time to reject as wrong passwords.
- **Access token:** 15-minute HS256 JWT.
- **Refresh token:** opaque, 256-bit, stored only as a SHA-256 hash.
  - It is replaced with a new one on every refresh.
  - Re-using an already-replaced token revokes all of that sign-in's tokens.
- **On Android:**
  - Tokens are encrypted with Tink AES-256-GCM, using a key held in Android Keystore, and are excluded from backups.
  - The app retries a request after a 401 by refreshing the token once (one refresh shared by concurrent requests).
  - A rejected refresh signs the user out with an "expired" notice.
- **Sign-out and account deletion** wipe tokens, the cached profile and the Room database. Account deletion re-asks for the password.
- Credential endpoints are rate-limited per IP (20 requests per 15 minutes).

## Backend: getting started

```bash
cd backend
cp .env.example .env
npm install
npm run dev      # http://localhost:3000/api/v1/health
npm test         # includes auth tests against an embedded Postgres
npm run db:generate   # after editing src/db/schema.ts: writes a new SQL migration to ./drizzle
```

Production (Cloud Run): build `backend/Dockerfile`, then set `DATABASE_URL` (Cloud SQL) and `JWT_SECRET` (48+ random bytes) from Secret Manager. Migrations run automatically on start.
