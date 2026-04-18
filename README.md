# Easy Transit

An Android app for Bloomington Transit built during the Luddy Hackathon. Shows live bus positions, schedules, trip planning, departure reminders, and trip ratings — all powered by the official BT GTFS and GTFS-Realtime feeds.

---

## Features

- **Live bus tracking** — vehicle positions polled every 10 seconds from the BT GTFS-RT feed
- **Schedule** — all 13 active BT routes with expandable stop times
- **Today / Tomorrow schedule** — browse next-day departures with a single tap
- **Arrival Planner** — pick any two stops and find the next direct trip with departure time, arrival time, and duration
- **Departure reminders** — tap the bell on any departure chip to get a notification 5 minutes before it leaves; survives device reboots
- **Route favorites** — star up to 3 routes to pin them to the top of your home screen
- **Arriving Near Me filter** — on the map, filter to only buses arriving within 500 m of you in the next 30 minutes
- **Pay Fare** — one-tap shortcut to the UMO fare payment app (or Play Store if not installed)
- **Trip ratings** — rate any trip you rode 1–5 stars with an optional comment
- **Service alerts** — real-time alerts from the BT alerts feed, auto-refreshed every 30 seconds
- **Destination search** — search stops and Bloomington landmarks

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| DI | Hilt |
| Database | Room (v3) |
| Networking | OkHttp |
| Realtime data | GTFS-Realtime (protobuf) |
| Maps | Google Maps Compose |
| Async | Kotlin Coroutines + Flow |
| Architecture | MVVM, Repository pattern |
| Notifications | AlarmManager + NotificationCompat |

---

## Data Sources

| Feed | URL | Refresh |
|------|-----|---------|
| Vehicle positions | `position_updates.pb` | Every 10s |
| Trip updates (ETAs) | `trip_updates.pb` | Every 10s |
| Service alerts | `alerts.pb` | Every 30s |
| Static GTFS (routes, stops, shapes) | `gtfs.zip` | On first launch |

All feeds hosted at:
`https://s3.amazonaws.com/etatransit.gtfs/bloomingtontransit.etaspot.net/`

---

## Active BT Routes

Routes 1, 2, 3, 4, 5, 6 (Campus Shuttle), 7, 9, 9 Limited, 11, 12, 13, 14

---

## Project Structure

```
app/src/main/java/com/example/bt_transit/
├── data/
│   ├── local/                        # Room DB (v3), entities, DAOs
│   │   ├── BTDatabase.kt
│   │   ├── dao/                      # StopDao, StopTimeDao, RatingDao, ...
│   │   └── entity/                   # StopEntity, RatingEntity, ...
│   ├── remote/                       # GtfsRtClient, GtfsStaticClient, WeatherClient
│   └── repository/                   # TransitRepository, RealtimeRepository,
│                                     # FavoritesRepository, RatingRepository
├── di/                               # NetworkModule, DatabaseModule (Hilt)
├── domain/model/                     # Vehicle, Route, Stop, TripUpdate, ...
├── notifications/                    # TransitNotificationManager,
│                                     # ReminderScheduler, ReminderReceiver, BootReceiver
├── ui/
│   ├── home/                         # Live bus count, nearby routes, favorites
│   ├── map/                          # Live map, arriving filter, trip ratings
│   ├── schedule/                     # Departures, arrival planner, reminders
│   ├── alerts/                       # Service alerts
│   ├── search/                       # Stop + landmark search
│   └── theme/                        # BT brand colors
└── util/
    └── FareAppLauncher.kt            # UMO deep-link / Play Store fallback
```

---

## Setup

1. Clone the repo
2. Open in Android Studio
3. Add `MAPS_API_KEY=<your_key>` to `local.properties` (root of repo) — without it the map renders but Google Maps tiles won't load
4. Run on emulator or device (API 26+)

No other API keys required — BT GTFS feeds are public.

---

## Team

4-person team, Luddy Hackathon.
