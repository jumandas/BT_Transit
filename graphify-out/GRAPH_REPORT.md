# Graph Report - .  (2026-04-18)

## Corpus Check
- Corpus is ~16,300 words - fits in a single context window. You may not need a graph.

## Summary
- 381 nodes · 347 edges · 70 communities detected
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 24 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_App Architecture & Context|App Architecture & Context]]
- [[_COMMUNITY_GTFS Data Repository|GTFS Data Repository]]
- [[_COMMUNITY_Map ViewModel|Map ViewModel]]
- [[_COMMUNITY_App Launcher Icon Round|App Launcher Icon Round]]
- [[_COMMUNITY_Home Screen UI|Home Screen UI]]
- [[_COMMUNITY_App Launcher Icon Standard|App Launcher Icon Standard]]
- [[_COMMUNITY_Search & Recommendations|Search & Recommendations]]
- [[_COMMUNITY_Service Alerts UI|Service Alerts UI]]
- [[_COMMUNITY_Stop Database DAO|Stop Database DAO]]
- [[_COMMUNITY_Stop Time DAO|Stop Time DAO]]
- [[_COMMUNITY_Database DI Module|Database DI Module]]
- [[_COMMUNITY_Home ViewModel|Home ViewModel]]
- [[_COMMUNITY_Map Screen UI|Map Screen UI]]
- [[_COMMUNITY_Search Screen UI|Search Screen UI]]
- [[_COMMUNITY_Room Database|Room Database]]
- [[_COMMUNITY_Schedule Screen UI|Schedule Screen UI]]
- [[_COMMUNITY_Waypoint DAO|Waypoint DAO]]
- [[_COMMUNITY_Waypoint Repository|Waypoint Repository]]
- [[_COMMUNITY_Schedule ViewModel|Schedule ViewModel]]
- [[_COMMUNITY_Route DAO|Route DAO]]
- [[_COMMUNITY_Trip DAO|Trip DAO]]
- [[_COMMUNITY_GTFS-RT API Client|GTFS-RT API Client]]
- [[_COMMUNITY_Network DI Module|Network DI Module]]
- [[_COMMUNITY_Stop Timeline Component|Stop Timeline Component]]
- [[_COMMUNITY_Default Launcher Icon Assets|Default Launcher Icon Assets]]
- [[_COMMUNITY_Round Icon HDPI Assets|Round Icon HDPI Assets]]
- [[_COMMUNITY_GTFS Static Client|GTFS Static Client]]
- [[_COMMUNITY_Weather API Client|Weather API Client]]
- [[_COMMUNITY_Arrival Proximity Watcher|Arrival Proximity Watcher]]
- [[_COMMUNITY_Shape DAO|Shape DAO]]
- [[_COMMUNITY_Transit Notifications|Transit Notifications]]
- [[_COMMUNITY_Waypoint ViewModel|Waypoint ViewModel]]
- [[_COMMUNITY_Instrumented Tests|Instrumented Tests]]
- [[_COMMUNITY_App Entry Point|App Entry Point]]
- [[_COMMUNITY_Main Activity|Main Activity]]
- [[_COMMUNITY_Bus Map Marker|Bus Map Marker]]
- [[_COMMUNITY_Stop Map Marker|Stop Map Marker]]
- [[_COMMUNITY_Navigation Graph|Navigation Graph]]
- [[_COMMUNITY_Route Color Utilities|Route Color Utilities]]
- [[_COMMUNITY_Unit Tests|Unit Tests]]
- [[_COMMUNITY_Route Entity|Route Entity]]
- [[_COMMUNITY_Shape Entity|Shape Entity]]
- [[_COMMUNITY_Stop Entity|Stop Entity]]
- [[_COMMUNITY_Stop Time Entity|Stop Time Entity]]
- [[_COMMUNITY_Trip Entity|Trip Entity]]
- [[_COMMUNITY_Waypoint Entity|Waypoint Entity]]
- [[_COMMUNITY_Stop Route Join|Stop Route Join]]
- [[_COMMUNITY_Stop Time Join|Stop Time Join]]
- [[_COMMUNITY_Realtime Repository|Realtime Repository]]
- [[_COMMUNITY_Weather Repository|Weather Repository]]
- [[_COMMUNITY_Geo Point Model|Geo Point Model]]
- [[_COMMUNITY_Route Domain Model|Route Domain Model]]
- [[_COMMUNITY_Scheduled Stop Model|Scheduled Stop Model]]
- [[_COMMUNITY_Service Alert Model|Service Alert Model]]
- [[_COMMUNITY_Stop Domain Model|Stop Domain Model]]
- [[_COMMUNITY_Stop Time Update|Stop Time Update]]
- [[_COMMUNITY_Trip Update Model|Trip Update Model]]
- [[_COMMUNITY_Vehicle Model|Vehicle Model]]
- [[_COMMUNITY_Waypoint Model|Waypoint Model]]
- [[_COMMUNITY_Weather Info Model|Weather Info Model]]
- [[_COMMUNITY_Alerts ViewModel|Alerts ViewModel]]
- [[_COMMUNITY_Profile Screen|Profile Screen]]
- [[_COMMUNITY_App Theme|App Theme]]
- [[_COMMUNITY_App Build Config|App Build Config]]
- [[_COMMUNITY_Project Settings|Project Settings]]
- [[_COMMUNITY_Root Build Config|Root Build Config]]
- [[_COMMUNITY_Color Definitions|Color Definitions]]
- [[_COMMUNITY_Typography|Typography]]
- [[_COMMUNITY_Main Activity Duplicate|Main Activity Duplicate]]
- [[_COMMUNITY_BT Brand Theme|BT Brand Theme]]

## God Nodes (most connected - your core abstractions)
1. `TransitRepository` - 20 edges
2. `BT Transit Android App` - 16 edges
3. `Round App Launcher Icon` - 15 edges
4. `MapViewModel` - 12 edges
5. `App Launcher Icon` - 12 edges
6. `StopDao` - 9 edges
7. `StopTimeDao` - 8 edges
8. `DatabaseModule` - 8 edges
9. `SearchViewModel` - 8 edges
10. `BTDatabase` - 7 edges

## Surprising Connections (you probably didn't know these)
- `App Launcher Icon` --has_background--> `Green Rounded Square Background`  [EXTRACTED]
  app/src/main/res/mipmap-mdpi/ic_launcher.webp → app/src/main/res/mipmap-xhdpi/ic_launcher.webp
- `App Launcher Icon` --belongs_to_density_bucket--> `mipmap-xhdpi Density Bucket`  [EXTRACTED]
  app/src/main/res/mipmap-mdpi/ic_launcher.webp → app/src/main/res/mipmap-xhdpi/ic_launcher.webp
- `App Launcher Icon` --has_background--> `Green Grid Background`  [EXTRACTED]
  app/src/main/res/mipmap-mdpi/ic_launcher.webp → app/src/main/res/mipmap-xxhdpi/ic_launcher.webp
- `App Launcher Icon` --has_visual_effect--> `Drop Shadow Effect`  [EXTRACTED]
  app/src/main/res/mipmap-mdpi/ic_launcher.webp → app/src/main/res/mipmap-xxhdpi/ic_launcher.webp
- `App Launcher Icon` --has_shape--> `Rounded Rectangle Icon Shape`  [EXTRACTED]
  app/src/main/res/mipmap-mdpi/ic_launcher.webp → app/src/main/res/mipmap-xxhdpi/ic_launcher.webp

## Communities

### Community 0 - "App Architecture & Context"
Cohesion: 0.1
Nodes (34): MVVM Architecture, Repository Pattern, Bloomington Transit (BT), BT Routes (1,2,3,4,5,6,7,9,9L,11,12,13,14), BT Transit Android App, AppNavigation, BTTransitApplication (App singleton), GtfsRtClient (fetches + parses .pb feeds) (+26 more)

### Community 1 - "GTFS Data Repository"
Cohesion: 0.07
Nodes (1): TransitRepository

### Community 2 - "Map ViewModel"
Cohesion: 0.11
Nodes (5): MapViewModel, SelectedBusInfo, SelectedStopInfo, StopArrivalLine, TimelineStop

### Community 3 - "App Launcher Icon Round"
Cohesion: 0.2
Nodes (14): Android Application, Android Robot Mascot, Round App Launcher Icon, BT Transit Android Application, Green Background with Grid Pattern, Drop Shadow Effect, Green Background with Grid Pattern, Green Background Color (+6 more)

### Community 4 - "Home Screen UI"
Cohesion: 0.15
Nodes (0): 

### Community 5 - "App Launcher Icon Standard"
Cohesion: 0.23
Nodes (13): Android Robot Mascot, App Launcher Icon, Green Grid Background, BT Transit Android Application, Green Color Scheme, Default Android Launcher Icon, Drop Shadow Effect, Green Rounded Square Background (+5 more)

### Community 6 - "Search & Recommendations"
Cohesion: 0.17
Nodes (4): RecommendationState, RouteRecommendation, SearchResult, SearchViewModel

### Community 7 - "Service Alerts UI"
Cohesion: 0.18
Nodes (2): AlertSeverity, AlertStyle

### Community 8 - "Stop Database DAO"
Cohesion: 0.2
Nodes (1): StopDao

### Community 9 - "Stop Time DAO"
Cohesion: 0.22
Nodes (1): StopTimeDao

### Community 10 - "Database DI Module"
Cohesion: 0.22
Nodes (1): DatabaseModule

### Community 11 - "Home ViewModel"
Cohesion: 0.22
Nodes (2): HomeViewModel, NearbyRouteUiModel

### Community 12 - "Map Screen UI"
Cohesion: 0.22
Nodes (0): 

### Community 13 - "Search Screen UI"
Cohesion: 0.22
Nodes (0): 

### Community 14 - "Room Database"
Cohesion: 0.25
Nodes (1): BTDatabase

### Community 15 - "Schedule Screen UI"
Cohesion: 0.25
Nodes (0): 

### Community 16 - "Waypoint DAO"
Cohesion: 0.29
Nodes (1): WaypointDao

### Community 17 - "Waypoint Repository"
Cohesion: 0.29
Nodes (1): WaypointRepository

### Community 18 - "Schedule ViewModel"
Cohesion: 0.29
Nodes (2): ScheduleViewModel, TripScheduleDetail

### Community 19 - "Route DAO"
Cohesion: 0.33
Nodes (1): RouteDao

### Community 20 - "Trip DAO"
Cohesion: 0.33
Nodes (1): TripDao

### Community 21 - "GTFS-RT API Client"
Cohesion: 0.33
Nodes (1): GtfsRtClient

### Community 22 - "Network DI Module"
Cohesion: 0.33
Nodes (1): NetworkModule

### Community 23 - "Stop Timeline Component"
Cohesion: 0.33
Nodes (1): StopState

### Community 24 - "Default Launcher Icon Assets"
Cohesion: 0.53
Nodes (6): Android Robot Mascot (Bugdroid), App Launcher Icon (ic_launcher), BT Transit Android Application, Default Android Studio Launcher Icon, Green Rounded-Square Background, Subtle Grid Pattern Overlay

### Community 25 - "Round Icon HDPI Assets"
Cohesion: 0.4
Nodes (6): Android Robot / Bugdroid Mascot, App Launcher Icon (Round), BT Transit Android App, Green Background Color, HDPI Mipmap Resource Density, Round Icon Shape

### Community 26 - "GTFS Static Client"
Cohesion: 0.4
Nodes (1): GtfsStaticClient

### Community 27 - "Weather API Client"
Cohesion: 0.4
Nodes (1): WeatherClient

### Community 28 - "Arrival Proximity Watcher"
Cohesion: 0.4
Nodes (1): ArrivalWatcher

### Community 29 - "Shape DAO"
Cohesion: 0.5
Nodes (1): ShapeDao

### Community 30 - "Transit Notifications"
Cohesion: 0.5
Nodes (1): TransitNotificationManager

### Community 31 - "Waypoint ViewModel"
Cohesion: 0.5
Nodes (1): WaypointViewModel

### Community 32 - "Instrumented Tests"
Cohesion: 0.67
Nodes (1): ExampleInstrumentedTest

### Community 33 - "App Entry Point"
Cohesion: 0.67
Nodes (1): BTApplication

### Community 34 - "Main Activity"
Cohesion: 0.67
Nodes (1): MainActivity

### Community 35 - "Bus Map Marker"
Cohesion: 0.67
Nodes (0): 

### Community 36 - "Stop Map Marker"
Cohesion: 0.67
Nodes (0): 

### Community 37 - "Navigation Graph"
Cohesion: 0.67
Nodes (1): NavItem

### Community 38 - "Route Color Utilities"
Cohesion: 0.67
Nodes (0): 

### Community 39 - "Unit Tests"
Cohesion: 0.67
Nodes (1): ExampleUnitTest

### Community 40 - "Route Entity"
Cohesion: 1.0
Nodes (1): RouteEntity

### Community 41 - "Shape Entity"
Cohesion: 1.0
Nodes (1): ShapeEntity

### Community 42 - "Stop Entity"
Cohesion: 1.0
Nodes (1): StopEntity

### Community 43 - "Stop Time Entity"
Cohesion: 1.0
Nodes (1): StopTimeEntity

### Community 44 - "Trip Entity"
Cohesion: 1.0
Nodes (1): TripEntity

### Community 45 - "Waypoint Entity"
Cohesion: 1.0
Nodes (1): WaypointEntity

### Community 46 - "Stop Route Join"
Cohesion: 1.0
Nodes (1): StopRoutePair

### Community 47 - "Stop Time Join"
Cohesion: 1.0
Nodes (1): StopTimeWithStop

### Community 48 - "Realtime Repository"
Cohesion: 1.0
Nodes (1): RealtimeRepository

### Community 49 - "Weather Repository"
Cohesion: 1.0
Nodes (1): WeatherRepository

### Community 50 - "Geo Point Model"
Cohesion: 1.0
Nodes (1): GeoPoint

### Community 51 - "Route Domain Model"
Cohesion: 1.0
Nodes (1): Route

### Community 52 - "Scheduled Stop Model"
Cohesion: 1.0
Nodes (1): ScheduledStop

### Community 53 - "Service Alert Model"
Cohesion: 1.0
Nodes (1): ServiceAlert

### Community 54 - "Stop Domain Model"
Cohesion: 1.0
Nodes (1): Stop

### Community 55 - "Stop Time Update"
Cohesion: 1.0
Nodes (1): StopTimeUpdate

### Community 56 - "Trip Update Model"
Cohesion: 1.0
Nodes (1): TripUpdate

### Community 57 - "Vehicle Model"
Cohesion: 1.0
Nodes (1): Vehicle

### Community 58 - "Waypoint Model"
Cohesion: 1.0
Nodes (1): Waypoint

### Community 59 - "Weather Info Model"
Cohesion: 1.0
Nodes (1): WeatherInfo

### Community 60 - "Alerts ViewModel"
Cohesion: 1.0
Nodes (1): AlertsViewModel

### Community 61 - "Profile Screen"
Cohesion: 1.0
Nodes (0): 

### Community 62 - "App Theme"
Cohesion: 1.0
Nodes (0): 

### Community 63 - "App Build Config"
Cohesion: 1.0
Nodes (0): 

### Community 64 - "Project Settings"
Cohesion: 1.0
Nodes (0): 

### Community 65 - "Root Build Config"
Cohesion: 1.0
Nodes (0): 

### Community 66 - "Color Definitions"
Cohesion: 1.0
Nodes (0): 

### Community 67 - "Typography"
Cohesion: 1.0
Nodes (0): 

### Community 68 - "Main Activity Duplicate"
Cohesion: 1.0
Nodes (1): MainActivity

### Community 69 - "BT Brand Theme"
Cohesion: 1.0
Nodes (1): Theme (BT brand colors, Inter font)

## Ambiguous Edges - Review These
- `Map UI (Map screen placeholder)` → `Rationale: 24-Hour Hackathon Constraint`  [AMBIGUOUS]
  README.md · relation: rationale_for

## Knowledge Gaps
- **58 isolated node(s):** `RouteEntity`, `ShapeEntity`, `StopEntity`, `StopTimeEntity`, `TripEntity` (+53 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Route Entity`** (2 nodes): `RouteEntity.kt`, `RouteEntity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Shape Entity`** (2 nodes): `ShapeEntity.kt`, `ShapeEntity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Stop Entity`** (2 nodes): `StopEntity.kt`, `StopEntity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Stop Time Entity`** (2 nodes): `StopTimeEntity.kt`, `StopTimeEntity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Trip Entity`** (2 nodes): `TripEntity.kt`, `TripEntity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Waypoint Entity`** (2 nodes): `WaypointEntity.kt`, `WaypointEntity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Stop Route Join`** (2 nodes): `StopRoutePair.kt`, `StopRoutePair`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Stop Time Join`** (2 nodes): `StopTimeWithStop.kt`, `StopTimeWithStop`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Realtime Repository`** (2 nodes): `RealtimeRepository.kt`, `RealtimeRepository`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Weather Repository`** (2 nodes): `WeatherRepository.kt`, `WeatherRepository`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Geo Point Model`** (2 nodes): `GeoPoint.kt`, `GeoPoint`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Route Domain Model`** (2 nodes): `Route.kt`, `Route`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Scheduled Stop Model`** (2 nodes): `ScheduledStop.kt`, `ScheduledStop`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Service Alert Model`** (2 nodes): `ServiceAlert.kt`, `ServiceAlert`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Stop Domain Model`** (2 nodes): `Stop.kt`, `Stop`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Stop Time Update`** (2 nodes): `StopTimeUpdate.kt`, `StopTimeUpdate`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Trip Update Model`** (2 nodes): `TripUpdate.kt`, `TripUpdate`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Vehicle Model`** (2 nodes): `Vehicle.kt`, `Vehicle`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Waypoint Model`** (2 nodes): `Waypoint.kt`, `Waypoint`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Weather Info Model`** (2 nodes): `WeatherInfo.kt`, `WeatherInfo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Alerts ViewModel`** (2 nodes): `AlertsViewModel`, `AlertsViewModel.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Profile Screen`** (2 nodes): `ProfileScreen.kt`, `ProfileScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Theme`** (2 nodes): `Theme.kt`, `BT_TransitTheme()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Build Config`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Project Settings`** (1 nodes): `settings.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Root Build Config`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Color Definitions`** (1 nodes): `Color.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Typography`** (1 nodes): `Type.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Main Activity Duplicate`** (1 nodes): `MainActivity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `BT Brand Theme`** (1 nodes): `Theme (BT brand colors, Inter font)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Map UI (Map screen placeholder)` and `Rationale: 24-Hour Hackathon Constraint`?**
  _Edge tagged AMBIGUOUS (relation: rationale_for) - confidence is low._
- **What connects `RouteEntity`, `ShapeEntity`, `StopEntity` to the rest of the system?**
  _58 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `App Architecture & Context` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._
- **Should `GTFS Data Repository` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Map ViewModel` be split into smaller, more focused modules?**
  _Cohesion score 0.11 - nodes in this community are weakly interconnected._