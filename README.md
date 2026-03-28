# Lama Lama Rangers — Lantana Control Field App (Android)

An Android field application built for the Lama Lama Rangers of Yintjingga Aboriginal Corporation (YAC), Cape York Peninsula, Queensland. Built as part of **31265 Communications for IT Professionals — EWB Challenge 2026**.

> This is a direct port of the [iOS app](https://github.com/username11384/ewbrangerapp) to Android, maintaining feature parity across both platforms.

---

## Overview

Lantana camara is one of the most invasive weeds in Australia. This app gives Lama Lama Rangers the tools to log sightings, coordinate treatment, track patrol coverage, and sync records across the team — all without a reliable internet connection.

---

## Features

### Map
- Satellite and standard map views centred on Port Stewart (osmdroid)
- Sighting pins colour-coded by Lantana variant
- Infestation zone polygons with status overlays (active / under treatment / cleared)
- Patrol area markers
- Layer toggles for sightings, zones, and patrols
- Draw mode for capturing new zone boundaries

### Sighting Log
- Log new sightings with GPS capture, variant picker, infestation size, and up to 3 photos
- Full sighting history with ranger name, relative timestamp, and sync status
- Sighting detail with linked treatment records
- Swipe to delete

### Treatment Records
- Log treatment method (cut stump, splat gun, foliar spray, basal bark), herbicide product, outcome notes, and optional follow-up date
- Treatment history linked to individual sightings

### Patrol
- Start a patrol with a checklist of pre-departure tasks
- Record patrol area, duration, and notes
- Calendar and list views of past patrols

### Variant Guide
- Reference cards for all six Lantana camara variants found in the region
- Identifying features, recommended control methods with instructions, and seasonal notes
- Biocontrol warning banner for pink Lantana during the wet season (Nov–Mar)

### Pesticide Inventory
- Track stock levels for herbicide products
- Log usage against treatment records
- Low-stock alerts when quantity falls below threshold

### Tasks
- Assign follow-up tasks with priority levels and due dates
- Filter by priority; swipe to delete
- Overdue task banner

### Mesh Sync (End of Day Sync)
- Peer-to-peer Bluetooth/WiFi sync between ranger devices via Google Nearby Connections
- No internet required — designed for remote field conditions

### Dashboard
- Sightings and treatment statistics
- Zone status breakdown
- Open follow-up tasks
- Sightings by ranger

---

## Project Structure

```
app/src/main/java/org/yac/llamarangers/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database + TypeConverters
│   │   ├── dao/                    # DAO interfaces per entity
│   │   └── entity/                 # Room entity classes
│   └── repository/                 # Repository implementations (SightingRepository, ZoneRepository, etc.)
├── domain/
│   └── model/
│       ├── enums/                  # LantanaVariant, InfestationSize, TreatmentMethod, SyncStatus, TaskPriority
│       └── SeasonalAlert.kt        # Seasonal guidance logic
├── resources/
│   └── PortStewartZones.kt         # Hardcoded patrol area coordinates and zone data
├── services/
│   ├── auth/                       # PIN-based auth + EncryptedSharedPreferences
│   ├── location/                   # FusedLocationProviderClient wrapper
│   └── sync/                       # NearbyConnectionsManager (Nearby Connections API)
├── ui/
│   ├── app/                        # MainTabScreen, MoreScreen, AppNavigation
│   ├── components/                 # Shared composables (VariantColourDot, SeasonalAlertBanner, etc.)
│   ├── login/                      # LoginScreen with PIN keypad
│   ├── map/                        # MapScreen, ZoneListScreen, ZoneDetailScreen, AddZoneScreen
│   ├── more/                       # DashboardScreen, MeshSyncScreen, SettingsScreen, VariantGuideScreen, etc.
│   ├── patrol/                     # PatrolScreen, ActivePatrolScreen
│   ├── sighting/                   # SightingListScreen, SightingDetailScreen, LogSightingScreen
│   ├── tasks/                      # TaskListScreen, AddTaskScreen
│   └── theme/                      # M3 colour scheme, typography
├── AppEnvironment.kt               # Hilt-injected app initialiser
├── AppNavigation.kt                # Compose NavHost with all routes
├── AuthManager.kt                  # Session management
└── DemoSeeder.kt                   # Demo branch: pre-seeded realistic data
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material Design 3 |
| Navigation | Compose Navigation |
| Local database | Room (SQLite) |
| Dependency injection | Hilt |
| Async | Kotlin Coroutines + StateFlow |
| Maps | osmdroid (no API key required) |
| Location | FusedLocationProviderClient |
| Peer sync | Google Nearby Connections API |
| Auth storage | EncryptedSharedPreferences |
| Camera | CameraX |

---

## Branches

| Branch | Purpose |
|---|---|
| `main` | Demo build — pre-seeded with realistic data, simulated mesh sync animation |

---

## Requirements

- Android Studio Meerkat or later
- Android SDK 35 (compile), minSdk 26 (Android 8.0+)
- JDK 17 (use Android Studio's bundled JDR — see below)
- No external API keys required

---

## Building

### Java version

The project requires JDK 17. If your system Java is newer (e.g. Java 25), add this to `gradle.properties`:

```properties
org.gradle.java.home=/Applications/Android Studio.app/Contents/jbr/Contents/Home
```

### Run the app

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and press Run.

---

## Running the Demo

1. Clone this repo and open in Android Studio
2. Build and run on an emulator or device (API 26+)
3. Log in as any ranger — PIN: `1234` for all demo accounts
4. Data is pre-seeded on first launch: 6 zones, 28 sightings, 10 patrols, pesticide stocks, and tasks
5. To reset: Settings → Reset App Data

Demo rangers:
- **Alice Johnson** — Senior Ranger
- **Bob Smith** — Ranger
- **Carol White** — Ranger

---

## Academic Context

This app was developed for the **EWB Challenge 2026** as part of unit **31265 Communications for IT Professionals** at UTS. The EWB (Engineers Without Borders) Challenge pairs university students with community organisations to address real development needs.

**Partner organisation:** Yintjingga Aboriginal Corporation (YAC), Port Stewart, Cape York Peninsula, QLD
**Problem domain:** Lantana camara weed management on Lama Lama Country
