# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

Build and install debug APK:
```bash
./gradlew installDebug
```

Run all tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

Run a single test class:
```bash
./gradlew test --tests "com.example.bt_transit.SomeTest"
```

## Setup

Add `MAPS_API_KEY=<your_key>` to `local.properties` (root of repo). Without it the map renders but Google Maps tiles won't load. No other API keys are needed — BT GTFS feeds are public.

## Architecture

MVVM + Repository pattern with Hilt DI.

### Data flow

**Static GTFS** (routes, stops, trips, shapes, stop_times) is downloaded once on first launch from a remote `gtfs.zip` via `GtfsStaticClient`, parsed as CSV, and inserted into a Room database (`BTDatabase`). `TransitRepository` wraps all Room queries and exposes domain models.

**GTFS-Realtime** feeds (vehicle positions, trip updates, service alerts) are protobuf `.pb` files fetched by `GtfsRtClient`. `RealtimeRepository` wraps these in polling Flows (10 s for positions/trips, 30 s for alerts).

### Key layers

| Package | Purpose |
|---------|---------|
| `data/remote/` | `GtfsRtClient` (RT protobuf), `GtfsStaticClient` (zip+CSV), `WeatherClient` |
| `data/local/` | Room database, entities (Route, Stop, Trip, StopTime, Shape, Waypoint), DAOs |
| `data/repository/` | `TransitRepository` (static), `RealtimeRepository` (RT), `WaypointRepository`, `WeatherRepository` |
| `domain/model/` | Pure Kotlin data classes: `Vehicle`, `TripUpdate`, `ServiceAlert`, `Stop`, `Route`, `ScheduledStop`, `Waypoint`, `WeatherInfo` |
| `di/` | `NetworkModule` (OkHttpClient, clients), `DatabaseModule` (Room) |
| `ui/<screen>/` | Each screen has a `Screen.kt` (Compose) + `ViewModel.kt` |
| `notifications/` | `TransitNotificationManager`, `ArrivalWatcher` |

### Navigation

`AppNavigation.kt` hosts a `NavHost` with a bottom bar. Routes:
- `home` → `HomeScreen`
- `map?routeId={routeId}` → `MapScreen` (routeId optional, navigated to from Home/Search)
- `schedule` → `ScheduleScreen`
- `alerts` → `AlertsScreen`
- `search` → `SearchScreen` (no bottom bar)

### Room database

`BTDatabase` version 2, entities: `StopEntity`, `RouteEntity`, `TripEntity`, `StopTimeEntity`, `ShapeEntity`, `WaypointEntity`. Increment the version and provide a migration when adding/changing entities.

### GTFS-RT feed URLs

All hosted at `https://s3.amazonaws.com/etatransit.gtfs/bloomingtontransit.etaspot.net/`:
- `position_updates.pb` — vehicle positions
- `trip_updates.pb` — ETAs per stop
- `alerts.pb` — service alerts
