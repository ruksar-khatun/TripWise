# Tasks - TripWise Data Accuracy & Google Places SDK Migration

- [x] Google Places SDK Integration (`build.gradle.kts`, `MainActivity.kt`, `TravelRepository.kt`)
  - [x] Add `com.google.android.libraries.places:places:3.4.0` dependency
  - [x] Initialize `Places` SDK in `MainActivity.kt`
  - [x] Update `TravelRepository.kt` to use `PlacesClient` for `fetchPlaceDetailsUsingSdk`
- [x] Deep Content Injection (`DestinationsFactory.kt`, `Models.kt`)
  - [x] Add `about`, `whyVisit`, `thingsToDo` to `Destination` model
  - [x] Expand all 30 destinations in `DestinationsFactory.kt` with real, rich content
- [x] Screen Synchronization & Profile Photo Fix (`DestinationDetailsScreen.kt`, `HomeScreen.kt`, `ProfileScreen.kt`)
  - [x] Connect new rich fields and Places SDK data in `DestinationDetailsScreen.kt`
  - [x] Fix profile image synchronization in `HomeScreen.kt` using `AsyncImage` and persistent `userProfile.avatarUri`
- [x] Verification & Build Check
  - [x] Run `./gradlew app:assembleDebug` and verify successful build
