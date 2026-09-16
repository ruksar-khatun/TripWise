# Implementation Plan - TripWise Data Accuracy & Google Places SDK Migration

Migrate the project to use the official Google Places SDK for Android (instead of REST calls), implement highly detailed real-world destination descriptions, centralize the profile synchronization, and establish `AppState` / `DestinationsFactory` as the singular source of truth for all screens.

## User Review Required

> [!IMPORTANT]
> - **Google Places SDK Integration**: The official `com.google.android.libraries.places:places:3.4.0` dependency will be added to the project. The SDK will be initialized in `MainActivity` using the `MAPS_API_KEY` from `local.properties`.
> - **Rich Destination Content**: `DestinationsFactory` and `Models.kt` will be expanded to hold full-length real descriptions, "Why Visit", and "Things to Do", eradicating all mock/placeholder generic text.
> - **Single Source of Truth**: `DestinationDetailsScreen` will rely exactly on `appState.selectedDestination` ensuring Explore -> Details -> Saved Places are all perfectly synchronized.
> - **Profile Photo Fix**: Eradicating duplicate states for the profile picture and ensuring `HomeScreen` and `ProfileScreen` read exactly from the persistent `PreferenceManager` backend.

---

## Proposed Changes

### 1. Google Places SDK (`build.gradle.kts`, `MainActivity.kt`, `TravelRepository.kt`)
- **[MODIFY] [build.gradle.kts](file:///C:/Users/ruksa/AndroidStudioProjects/TripWise/app/build.gradle.kts)**: Add `implementation("com.google.android.libraries.places:places:3.4.0")`.
- **[MODIFY] [MainActivity.kt](file:///C:/Users/ruksa/AndroidStudioProjects/TripWise/app/src/main/java/com/example/tripwise/MainActivity.kt)**: Initialize `Places.initializeWithNewPlacesApiEnabled(applicationContext, BuildConfig.MAPS_API_KEY)`.
- **[MODIFY] [TravelRepository.kt](file:///C:/Users/ruksa/AndroidStudioProjects/TripWise/app/src/main/java/com/example/tripwise/data/network/TravelRepository.kt)**: Inject `PlacesClient` to implement `fetchPlaceDetailsUsingSdk()`. Query `Place.Field.RATING`, `Place.Field.USER_RATINGS_TOTAL`, `Place.Field.PRICE_LEVEL`, `Place.Field.TYPES` directly from the SDK rather than Retrofit.

### 2. Deep Content Injection (`DestinationsFactory.kt`, `Models.kt`)
- **[MODIFY] [Models.kt](file:///C:/Users/ruksa/AndroidStudioProjects/TripWise/app/src/main/java/com/example/tripwise/data/Models.kt)**: Add `about: String`, `whyVisit: String`, `thingsToDo: List<String>` to the `Destination` class.
- **[MODIFY] [DestinationsFactory.kt](file:///C:/Users/ruksa/AndroidStudioProjects/TripWise/app/src/main/java/com/example/tripwise/data/DestinationsFactory.kt)**: Expand all 30 Asian destinations with real, multi-paragraph factual travel info (sourced accurately).

### 3. Screen Synchronisation (`DestinationDetailsScreen.kt`, `HomeScreen.kt`)
- **[MODIFY] [DestinationDetailsScreen.kt](file:///C:/Users/ruksa/AndroidStudioProjects/TripWise/app/src/main/java/com/example/tripwise/ui/screens/home/DestinationDetailsScreen.kt)**: Wire up the rich description fields (About, Why Visit, Things to Do, Local Food) and the new Google Places SDK results. Ensure data reads from `appState.destinations` consistently.
- **[MODIFY] [HomeScreen.kt](file:///C:/Users/ruksa/AndroidStudioProjects/TripWise/app/src/main/java/com/example/tripwise/ui/screens/home/HomeScreen.kt)**: Fix the profile image persistence issue to ensure `appState.userProfile.avatarUri` drives the UI perfectly in sync with the Profile screen.

---

## Verification Plan

### Automated Tests
- Run Gradle assemble task: `./gradlew app:assembleDebug`

### Manual Verification
- Edit profile photo via Device Picker -> Verify immediate update on both `Profile` and `Home` header. Restart app to confirm persistence.
- Open "Munnar Green Hills" from Explore -> Verify deep, specific content (Not just 1 line). Check the Places SDK live injection of Rating and Reviews.
- Verify Offline Fallback -> Turn off internet, open a destination, and ensure the app loads the rich local data smoothly without crashing.
