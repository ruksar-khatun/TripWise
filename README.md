# TripWise – Smart Travel Planner

TripWise is a premium, modern, and minimal Android mobile application designed to simplify travel planning. Built with **Kotlin** and **Jetpack Compose**, it offers a seamless user experience for discovering Asian destinations, managing travel budgets, and organizing day-by-day itineraries.

## 🚀 Features

### 🌍 Discovery & Exploration
- **30+ Asian Destinations**: Curated list of top locations across India, Japan, South Korea, Thailand, Singapore, and more.
- **Rich Content**: Detailed destination descriptions, "Why Visit" sections, and must-try local food recommendations.
- **Verified HD Imagery**: Real-world high-definition photographs for every location, stored locally for offline access.
- **Google Places Integration**: Live ratings, review counts, and location details fetched directly from the official Google Places SDK.
- **Real-Time Weather**: Dynamic weather updates (temperature, conditions, wind) for selected destinations via the Open-Meteo API.

### 💰 Smart Budgeting & Planning
- **Total Budget Entry**: Start planning by defining your total available funds in your preferred currency.
- **7-Category Allocation**: Intelligently distribute your budget across Transportation, Accommodation, Food, Activities, Local Travel, Shopping, and Miscellaneous.
- **Currency Conversion**: Support for 50+ Asian currencies (INR, JPY, KRW, AED, etc.) with real-time exchange rates via API integration.
- **Itinerary Management**: Day-by-day activity scheduling with time, cost, and completion tracking.

### 👤 Personalization
- **User Profile Sync**: Unified state management ensures your profile photo, name, and preferences are consistent across Home and Profile screens.
- **Device Photo Picker**: Upload custom profile pictures directly from your phone's gallery.
- **Dark Mode Support**: Full Material 3 Dark Theme support with Light, Dark, and System Default options.
- **Persistence**: Travel preferences, saved places, and planned trips are stored locally and remain available after app restarts.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material 3
- **Networking**: Retrofit & OkHttp
- **Image Loading**: Coil
- **External APIs**: 
  - Google Places SDK for Android
  - Open-Meteo (Weather)
  - ExchangeRate-API (Currency)
- **Data Persistence**: SharedPreferences & PreferenceManager
- **Navigation**: Jetpack Navigation Compose

## 🎨 Color Palette

- **Dusk Blue (#26538D)**: Primary text, icons, and navigation.
- **Azure Mist (#F0FFFF)**: Main application background.
- **Sea Blue (#4BAFC0)**: Primary buttons and action highlights.
- **Sea Salt (#8BAF9D)**: Secondary accents and borders.

## ⚙️ Setup & Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/tripwise.git
   ```
2. **Configure API Keys**:
   - Open `local.properties` in the root directory.
   - Add your Google Maps/Places API key:
     ```properties
     MAPS_API_KEY="YOUR_API_KEY_HERE"
     ```
3. **Build the project**:
   - Open the project in **Android Studio (Ladybug or newer)**.
   - Sync Project with Gradle Files.
   - Run the `:app` module on an emulator or physical device.

## 📁 Project Structure

```text
com.example.tripwise/
├── data/
│   ├── network/          # API Services & Repositories
│   ├── AppState.kt       # Central Reactive State Holder
│   ├── Models.kt         # Data Structures
│   └── DestinationsFactory.kt # Destination seeding
├── ui/
│   ├── components/       # Reusable UI widgets
│   ├── navigation/       # NavGraph & Route definitions
│   ├── screens/          # Feature-specific screens (Auth, Home, Trip, Profile)
│   └── theme/            # Color, Typography, and Theme definitions
└── MainActivity.kt       # App Entry Point
```

## 📝 Future AI Roadmap (CA3)
The data structures in TripWise are pre-architected to support future AI integration, including:
- Personalized destination recommendation engines.
- Automated AI itinerary generation based on user preferences and budget.
- AI-driven budget optimization and travel assistant.

---
Developed with ❤️ for a premium travel experience.
