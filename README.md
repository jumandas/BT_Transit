# BT Transit

An Android app for Bloomington Transit built during the Luddy Hackathon. Shows live bus positions, schedules, service alerts, and destination search — all powered by the official BT GTFS and GTFS-Realtime feeds.

---

## Features

- **Live bus tracking** — vehicle positions polled every 10 seconds from the BT GTFS-RT feed
- **Schedule** — all 13 active BT routes with expandable stop times
- **Service alerts** — real-time alerts from the BT alerts feed, auto-refreshed every 30 seconds
- **Destination search** — search Bloomington landmarks and saved places
- **Bottom navigation** — Home, Map, Schedule, Alerts

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Networking | OkHttp |
| Realtime data | GTFS-Realtime (protobuf) |
| Async | Kotlin Coroutines + Flow |
| Architecture | MVVM, Repository pattern |

---

## Data Sources

| Feed | URL | Refresh |
|------|-----|---------|
| Vehicle positions | `position_updates.pb` | Every 10s |
| Trip updates (ETAs) | `trip_updates.pb` | Every 10s |
| Service alerts | `alerts.pb` | Every 30s |
| Static GTFS (routes, stops, shapes) | `gtfs.zip` | On first launch |

All feeds are hosted at:
`https://s3.amazonaws.com/etatransit.gtfs/bloomingtontransit.etaspot.net/`

---

## Active BT Routes

Routes 1, 2, 3, 4, 5, 6 (Campus Shuttle), 7, 9, 9 Limited, 11, 12, 13, 14

---

## Project Structure

```
app/src/main/java/com/example/bt_transit/
├── BTTransitApplication.kt       # App singleton, holds repositories
├── MainActivity.kt
├── data/
│   ├── remote/GtfsRtClient.kt    # Fetches + parses all 3 .pb feeds
│   └── repository/
│       └── RealtimeRepository.kt # 10s polling Flows
├── domain/model/
│   └── Models.kt                 # Vehicle, TripUpdate, ServiceAlert
└── ui/
    ├── navigation/AppNavigation.kt
    ├── home/                     # Live bus count + nearby routes
    ├── schedule/                 # Expandable route + stop list
    ├── alerts/                   # Live service alerts
    ├── search/                   # Destination search
    ├── map/                      # (Map screen placeholder)
    └── theme/                    # BT brand colors, Inter font
```

---

## Setup

1. Clone the repo
2. Open in Android Studio
3. Run on emulator or device (API 26+)
4. No API keys required for GTFS-RT feeds

---

## Team

4-person team, Luddy Hackathon — 24 hours.
