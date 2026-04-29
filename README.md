# Lama Lama Rangers — Invasive Species Control Field App (Android)

An Android field application built for the Lama Lama Rangers of Yintjingga Aboriginal Corporation (YAC), Cape York Peninsula, Queensland. Built as part of **31265 Communications for IT Professionals — EWB Challenge 2026**.

> This is a strict 1:1 port of the [iOS app (v3)](https://github.com/username11384/ewbrangerapp) to Android, maintaining feature parity, demo data, and fake syncing workflows.

---

## Overview

Invasive weed management is critical for the health of Lama Lama Country. This app provides rangers with tools to log sightings of multiple invasive species, coordinate treatment, track patrol coverage, manage equipment, and sync records across the team — all designed for remote field conditions without internet.

---

## Features (V2 & V3 Parity)

### Map & Multi-Species
- **6 Invasive Species:** Support for Lantana, Rubber Vine, Prickly Acacia, Sicklepod, Giant Rat's Tail Grass, and Pond Apple.
- **Bloom Calendar:** Seasonal risk visualizer showing peak flowering/seeding months for all species.
- **Dynamic Map:** Satellite and standard views (osmdroid) with color-coded sighting pins and zone polygons.
- **Zone Overlays:** Real-time status (Active, Under Treatment, Cleared) with draw-mode for new boundaries.

### Field Records (V3 Enhanced)
- **Sighting Log:** Capture GPS, species type, infestation size estimate (m²), voice notes, and before/after photos.
- **Safety Check-In:** Periodic timer-based check-in system with SOS alerts for lone ranger safety.
- **Hazard Logger:** Log field hazards (wasps, snakes, flood damage) with GPS and photo documentation.
- **Treatment Records:** Track herbicide products, mechanical removal, stem injection, or fire management with outcome notes.
- **Before/After Comparison:** Visual comparison cards in sighting details to track treatment effectiveness.

### Hub & Operations
- **Hub Tab:** Central tile-grid for operational workflows (Dashboard, Day Sync, Zones, Cloud Sync).
- **Shift Handover:** Generate and share end-of-shift reports summarizing field activity and species breakdown.
- **Cloud Sync Demo:** Simulated live-sync dashboard demonstrating Supabase and S3 integration.
- **Conflict Resolver:** Interactive UI to resolve zone boundary conflicts during peer-to-peer sync.
- **Equipment Log:** Track gear maintenance, service history, and next maintenance due dates.

### Management & Guidance
- **Species Guide:** Full identification guide with distinguishing features and recommended control methods.
- **Pesticide Inventory:** Low-stock alerts and usage tracking against treatment records.
- **Tasks:** Assign follow-up tasks (e.g., regrowth checks) with priority-based filtering.
- **Patrol Checklists:** Per-area safety and gear checklists before starting a patrol.

---

## Project Structure

```
app/src/main/java/org/yac/llamarangers/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database (v2)
│   │   ├── dao/                    # 15+ DAOs including V3 (Safety, Hazard, Equipment)
│   │   └── entity/                 # Room entities mirroring CoreData models
│   └── repository/                 # Data logic (Sighting, Zone, Ranger, Task, etc.)
├── domain/
│   └── model/
│       └── enums/                  # InvasiveSpecies, TreatmentMethod, InfestationSize, etc.
├── resources/
│   ├── InvasiveSpeciesContent.kt   # Identification guide data
│   └── PortStewartZones.kt         # Geographic region definitions
├── services/
│   ├── auth/                       # PIN-based session management
│   ├── location/                   # GPS capture service
│   └── sync/                       # Nearby Connections (Mesh) logic
├── ui/
│   ├── hub/                        # Hub grid, Handover, Conflict, Cloud Sync screens
│   ├── map/                        # MapScreen, BloomCalendar, Zone management
│   ├── patrol/                     # Checklist and active tracking
│   ├── safety/                     # Safety timer and check-in UI (V3)
│   ├── sighting/                   # Logging, Detail, Before/After photos
│   └── theme/                      # Lama Lama Ranger design system
└── demo/
    └── DemoSeeder.kt               # Seeds 28+ records for 1:1 iOS parity
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material Design 3 |
| Navigation | Compose Navigation |
| Local database | Room (SQLite) with Destructive Migration for Demo |
| Dependency injection | Hilt |
| Async | Kotlin Coroutines + StateFlow |
| Maps | osmdroid |
| Media | CameraX + MediaRecorder (Voice Notes) |
| Peer sync | Google Nearby Connections API |

---

## Requirements

- Android Studio Meerkat or later
- JDK 17
- No external API keys required (Fully offline-capable)

---

## Building & Running

```bash
./gradlew assembleDebug
```

1. Log in with PIN: `1234`
2. First launch automatically seeds rich demo data (6 zones, 28 sightings, 10 patrols, maintenance logs).
3. Reset data anytime in **Hub → Settings → Reset App Data**.

---

## Academic Context

Developed for the UTS **EWB UTS Challenge 2026** (31265 Communications for IT Professionals).
**Partner:** Yintjingga Aboriginal Corporation (YAC)
**Country:** Lama Lama Country, Port Stewart, QLD
