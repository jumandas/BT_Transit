# New Features Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement six new features — UMO redirect, next-day schedule, favorites + arriving filter, arrival planner, departure reminder, and trip ratings — in the Bloomington Transit Android app.

**Architecture:** MVVM + Hilt DI. New business logic goes in Repository/ViewModel; UI-only helpers go in composable or util files. Room DB version bumps from v2 → v3 for trip ratings only; all other features use SharedPreferences or pure GTFS queries.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Room v2.6.1, AlarmManager, SharedPreferences, Google Maps Compose 4.4.1

---

## File Map

| Task | Files Created | Files Modified |
|------|--------------|----------------|
| 1 (F6) | `util/FareAppLauncher.kt` | `AndroidManifest.xml`, `HomeScreen.kt` |
| 2 (F5) | — | `ScheduleViewModel.kt`, `ScheduleScreen.kt` |
| 3 (F4a) | `data/repository/FavoritesRepository.kt` | — |
| 4 (F4b) | — | `HomeViewModel.kt`, `HomeScreen.kt` |
| 5 (F4c) | — | `StopTimeDao.kt`, `TransitRepository.kt`, `MapViewModel.kt`, `MapScreen.kt` |
| 6 (F2a) | `data/local/projection/DirectTripResult.kt` | `StopTimeDao.kt`, `TransitRepository.kt` |
| 7 (F2b) | — | `ScheduleViewModel.kt`, `ScheduleScreen.kt` |
| 8 (F7a) | `notifications/ReminderScheduler.kt`, `notifications/ReminderReceiver.kt`, `notifications/BootReceiver.kt` | `AndroidManifest.xml` |
| 9 (F7b) | — | `ScheduleViewModel.kt`, `ScheduleScreen.kt` |
| 10 (F3a) | `data/local/entity/RatingEntity.kt`, `data/local/dao/RatingDao.kt` | `BTDatabase.kt`, `DatabaseModule.kt` |
| 11 (F3b) | `data/repository/RatingRepository.kt` | `MapViewModel.kt`, `MapScreen.kt` |

All paths are relative to `app/src/main/java/com/example/bt_transit/` unless stated.

---

## Task 1: F6 — UMO Fare Redirect

**Files:**
- Create: `app/src/main/java/com/example/bt_transit/util/FareAppLauncher.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/example/bt_transit/ui/home/HomeScreen.kt`

> **IMPORTANT:** The UMO package name `com.cubic.ctp.app` must be verified against the live Play Store before shipping. Search for "UMO" or "Bloomington Transit" on Play Store, tap the app, and check the URL for the package name. Update the constant if it differs.

- [ ] **Step 1: Create FareAppLauncher.kt**

```kotlin
// app/src/main/java/com/example/bt_transit/util/FareAppLauncher.kt
package com.example.bt_transit.util

import android.content.Context
import android.content.Intent
import android.net.Uri

const val FARE_APP_PACKAGE = "com.cubic.ctp.app" // verify on Play Store before shipping

fun openFareApp(context: Context, pkg: String = FARE_APP_PACKAGE) {
    val intent = context.packageManager.getLaunchIntentForPackage(pkg)
        ?: Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
        )
    context.startActivity(intent)
}
```

- [ ] **Step 2: Add `<queries>` block to AndroidManifest.xml**

In `app/src/main/AndroidManifest.xml`, add the `<queries>` block as a direct child of `<manifest>`, BEFORE `<application>`:

```xml
<queries>
    <package android:name="com.cubic.ctp.app" />
</queries>
```

The file should look like:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <queries>
        <package android:name="com.cubic.ctp.app" />
    </queries>

    <application ...>
```

- [ ] **Step 3: Add Pay Fare button to HomeScreen.kt**

In `HomeScreen.kt`, add the following import at the top:
```kotlin
import androidx.compose.material.icons.filled.CreditCard
import com.example.bt_transit.util.openFareApp
```

In the `HeroSection` composable, replace the existing `Row` that contains `Column(modifier = Modifier.weight(1f))` and `WeatherBadge`:

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top
) {
    Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.DirectionsBus,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Easy Transit",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Hi there 👋",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
        Text(
            text = "Where to next?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
    Column(horizontalAlignment = Alignment.End) {
        weather?.let { WeatherBadge(it) }
        Spacer(Modifier.height(8.dp))
        val context = LocalContext.current
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
            modifier = Modifier.clickable { openFareApp(context) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = "Pay fare",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Pay Fare",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
```

- [ ] **Step 4: Build and verify**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL. If it fails on missing `ic_bus_notification` or other unrelated issues, those are pre-existing — only fix errors introduced by this task.

---

## Task 2: F5 — Next-Day Schedule

**Files:**
- Modify: `app/src/main/java/com/example/bt_transit/ui/schedule/ScheduleViewModel.kt`
- Modify: `app/src/main/java/com/example/bt_transit/ui/schedule/ScheduleScreen.kt`

- [ ] **Step 1: Add `selectedDay` state to ScheduleViewModel**

In `ScheduleViewModel.kt`, add these fields after the `_departures` declaration:

```kotlin
private val _selectedDay = MutableStateFlow(0) // 0 = today, 1 = tomorrow
val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()
```

Replace `loadDeparturesFor` with a version that is day-aware and busts the cache on day change:

```kotlin
fun setDay(day: Int) {
    if (_selectedDay.value == day) return
    _selectedDay.value = day
    _departures.value = emptyMap() // invalidate cache when day changes
}

fun loadDeparturesFor(routeId: String) {
    val day = _selectedDay.value
    val cacheKey = "$routeId:$day"
    if (cacheKey in _departures.value) return
    viewModelScope.launch {
        val timeStr = if (day == 0) currentTimeStr() else "00:00:00"
        val times = transitRepository.getNextDeparturesForRoute(routeId, timeStr, 6)
        _departures.update { it + (cacheKey to times) }
    }
}
```

Also update the `_departures` type to `Map<String, List<String>>` (cache key is now `"routeId:day"`).

- [ ] **Step 2: Update departures collection in ScheduleScreen**

`departures[route.routeId]` now needs to be `departures["${route.routeId}:$selectedDay"]`. In `ScheduleScreen.kt`, collect `selectedDay`:

```kotlin
val selectedDay by vm.selectedDay.collectAsStateWithLifecycle()
```

Pass `selectedDay` to `RouteRow` and the expand lambda:

```kotlin
items(routes, key = { it.routeId }) { route ->
    RouteRow(
        route = route,
        departureTimes = departures["${route.routeId}:$selectedDay"],
        onExpand = { vm.loadDeparturesFor(route.routeId) },
        onDepartureClick = { timeStr -> vm.openTripDetail(route, timeStr) }
    )
}
```

- [ ] **Step 3: Add Today/Tomorrow chips to ScheduleScreen**

In `ScheduleScreen.kt`, add these imports:
```kotlin
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Snackbar
```

Insert a `DayChipRow` composable just before the `LazyColumn` (inside the `else` branch, after `ScheduleHeader()`):

```kotlin
@Composable
private fun DayChipRow(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedDay == 0,
                onClick = { onDaySelected(0) },
                label = { Text("Today") }
            )
            FilterChip(
                selected = selectedDay == 1,
                onClick = { onDaySelected(1) },
                label = { Text("Tomorrow") }
            )
        }
        if (selectedDay == 1) {
            Text(
                text = "Schedule may vary on holidays and special service days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 4.dp)
            )
        }
    }
}
```

In `ScheduleScreen`, place `DayChipRow` right before `LazyColumn`:
```kotlin
DayChipRow(selectedDay = selectedDay, onDaySelected = { vm.setDay(it) })
LazyColumn(...)
```

- [ ] **Step 4: Build and verify**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

---

## Task 3: F4a — FavoritesRepository

**Files:**
- Create: `app/src/main/java/com/example/bt_transit/data/repository/FavoritesRepository.kt`

- [ ] **Step 1: Create FavoritesRepository.kt**

```kotlin
// app/src/main/java/com/example/bt_transit/data/repository/FavoritesRepository.kt
package com.example.bt_transit.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "bt_favorites"
private const val KEY_FAV_ROUTES = "fav_routes"
private const val MAX_FAVORITES = 3

@Singleton
class FavoritesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _favorites = MutableStateFlow(loadFromPrefs())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private fun loadFromPrefs(): Set<String> =
        prefs.getStringSet(KEY_FAV_ROUTES, emptySet()) ?: emptySet()

    fun isFavorite(routeId: String): Boolean = _favorites.value.contains(routeId)

    /** Returns false if already at max capacity and routeId is not already a favorite. */
    fun toggleFavorite(routeId: String): Boolean {
        val current = _favorites.value.toMutableSet()
        return if (current.contains(routeId)) {
            current.remove(routeId)
            save(current)
            true
        } else if (current.size < MAX_FAVORITES) {
            current.add(routeId)
            save(current)
            true
        } else {
            false // at capacity
        }
    }

    private fun save(set: Set<String>) {
        prefs.edit().putStringSet(KEY_FAV_ROUTES, set).apply()
        _favorites.value = set.toSet()
    }
}
```

- [ ] **Step 2: Build to verify Hilt can inject it**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL. Hilt will auto-discover `@Singleton` + `@Inject constructor` — no `@Provides` needed.

---

## Task 4: F4b — Favorites in Home Screen

**Files:**
- Modify: `app/src/main/java/com/example/bt_transit/ui/home/HomeViewModel.kt`
- Modify: `app/src/main/java/com/example/bt_transit/ui/home/HomeScreen.kt`

- [ ] **Step 1: Inject FavoritesRepository into HomeViewModel**

In `HomeViewModel.kt`, add `FavoritesRepository` to the constructor and expose `favorites`:

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transitRepo: TransitRepository,
    private val realtimeRepo: RealtimeRepository,
    private val weatherRepo: WeatherRepository,
    private val favoritesRepo: FavoritesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
```

Add the favorites flow after the existing `weather` field:
```kotlin
val favorites: StateFlow<Set<String>> = favoritesRepo.favorites
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())
```

Add a toggle method and an `atFavCapacity` method:
```kotlin
/** Returns false when already at 3 favorites and routeId is not yet starred. */
fun toggleFavorite(routeId: String): Boolean = favoritesRepo.toggleFavorite(routeId)
```

- [ ] **Step 2: Sort nearbyRoutes so favorites float to top**

In `HomeViewModel.kt`, modify `loadNearbyRoutes` at the end where `_nearbyRoutes.value = result` is set. Replace that line with:

```kotlin
_nearbyRoutes.value = sortWithFavorites(result)
_isLoadingNearby.value = false
```

Add the sort helper method inside `HomeViewModel`:
```kotlin
private fun sortWithFavorites(routes: List<NearbyRouteUiModel>): List<NearbyRouteUiModel> {
    val favs = favoritesRepo.favorites.value
    return routes.sortedWith(compareByDescending { it.routeId in favs })
}
```

Also update `refreshRealtimeEtas` to re-sort after updating ETAs:
```kotlin
private fun refreshRealtimeEtas(updates: List<TripUpdate>) {
    val now = System.currentTimeMillis() / 1000
    val updated = _nearbyRoutes.value.map { item ->
        val realtimeEpoch = updates
            .filter { it.routeId == item.routeId }
            .flatMap { tu -> tu.updates.filter { it.stopId == item.stopId } }
            .mapNotNull { it.arrivalEpochSec }
            .filter { it > now }
            .minOrNull()
        if (realtimeEpoch != null) {
            val diffMin = ((realtimeEpoch - now) / 60).toInt()
            item.copy(nextArrivalLabel = when {
                diffMin <= 0 -> "Now"
                diffMin == 1 -> "1 min"
                else -> "$diffMin min"
            })
        } else item
    }
    _nearbyRoutes.value = sortWithFavorites(updated)
}
```

- [ ] **Step 3: Add favorite icon to NearbyRouteCard in HomeScreen.kt**

In `HomeScreen.kt`, add to imports:
```kotlin
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
```

Update `HomeScreen` to collect favorites and pass a `onFavoriteToggle` callback:

```kotlin
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    onSearchClick: () -> Unit = {},
    onNearbyRouteClick: (String) -> Unit = {},
    vm: HomeViewModel = hiltViewModel()
) {
    val vehicles by vm.vehicles.collectAsStateWithLifecycle()
    val nearbyRoutes by vm.nearbyRoutes.collectAsStateWithLifecycle()
    val isLoadingNearby by vm.isLoadingNearby.collectAsStateWithLifecycle()
    val weather by vm.weather.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            try {
                @Suppress("MissingPermission")
                client.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) vm.onLocationGranted(loc.latitude, loc.longitude)
                }
            } catch (_: Exception) {}
        }
    }
    LaunchedEffect(Unit) {
        locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item { HeroSection(activeBusCount = vehicles.size, weather = weather) }
            item { FloatingSearchCard(onClick = onSearchClick) }
            item { QuickStatsRow(activeBuses = vehicles.size, nearbyCount = nearbyRoutes.size) }
            item { SectionHeader("Nearby Routes", subtitle = "Buses servicing stops near you") }
            item {
                when {
                    isLoadingNearby -> LoadingState()
                    nearbyRoutes.isEmpty() -> EmptyRoutesState()
                    else -> {
                        Column {
                            nearbyRoutes.forEach { route ->
                                NearbyRouteCard(
                                    route = route,
                                    isFavorite = route.routeId in favorites,
                                    onClick = { onNearbyRouteClick(route.routeId) },
                                    onFavoriteToggle = {
                                        val added = vm.toggleFavorite(route.routeId)
                                        if (!added) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Remove a favorite to add a new one"
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

Add `import kotlinx.coroutines.launch` and `import androidx.compose.runtime.rememberCoroutineScope`.

- [ ] **Step 4: Update NearbyRouteCard signature**

Replace the existing `NearbyRouteCard` composable with:

```kotlin
@Composable
private fun NearbyRouteCard(
    route: NearbyRouteUiModel,
    isFavorite: Boolean,
    onClick: () -> Unit = {},
    onFavoriteToggle: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(route.color), Color(route.color).copy(alpha = 0.78f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = route.shortName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.longName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = route.stopName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        maxLines = 1
                    )
                }
            }

            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
                    tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(4.dp))

            val isRealtime = route.nextArrivalLabel != "—"
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = route.nextArrivalLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isRealtime) Color(route.color)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
                if (isRealtime) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "next bus",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 5: Build and verify**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

---

## Task 5: F4c — Arriving-Near-Me Filter on Map

**Files:**
- Modify: `app/src/main/java/com/example/bt_transit/data/local/dao/StopTimeDao.kt`
- Modify: `app/src/main/java/com/example/bt_transit/data/repository/TransitRepository.kt`
- Modify: `app/src/main/java/com/example/bt_transit/ui/map/MapViewModel.kt`
- Modify: `app/src/main/java/com/example/bt_transit/ui/map/MapScreen.kt`

- [ ] **Step 1: Add `getActiveTripIds` query to StopTimeDao**

Append to `StopTimeDao.kt`:

```kotlin
@Query("""
    SELECT DISTINCT st.tripId FROM stop_times st
    WHERE st.stopId IN (:stopIds)
    AND st.arrivalTime >= :afterTime AND st.arrivalTime <= :beforeTime
""")
suspend fun getActiveTripIdsNearStops(
    stopIds: List<String>,
    afterTime: String,
    beforeTime: String
): List<String>
```

- [ ] **Step 2: Add `getActiveTripIdsNear` to TransitRepository**

Append to `TransitRepository.kt`:

```kotlin
/**
 * Returns tripIds that stop within [radiusMeters] of [lat]/[lng] between [afterTime] and [beforeTime] (HH:mm:ss).
 * Edge case: wrapping past midnight (e.g., 23:45 → 00:15) is not handled; results may be incomplete.
 */
suspend fun getActiveTripIdsNear(
    lat: Double,
    lng: Double,
    radiusMeters: Double = 500.0,
    afterTime: String,
    beforeTime: String
): Set<String> {
    val nearbyStops = db.stopDao().findNearest(lat, lng, 20)
        .filter { stop -> haversineMeters(lat, lng, stop.lat, stop.lng) <= radiusMeters }
    if (nearbyStops.isEmpty()) return emptySet()
    return db.stopTimeDao()
        .getActiveTripIdsNearStops(nearbyStops.map { it.stopId }, afterTime, beforeTime)
        .toSet()
}

private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2).pow(2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2).pow(2)
    return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}
```

Add `import kotlin.math.pow` at the top of `TransitRepository.kt`.

- [ ] **Step 3: Add arriving filter state to MapViewModel**

In `MapViewModel.kt`, after `_directionTripIds`:

```kotlin
private val _arrivingFilterEnabled = MutableStateFlow(false)
val arrivingFilterEnabled: StateFlow<Boolean> = _arrivingFilterEnabled.asStateFlow()

private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

private val _arrivingTripIds = MutableStateFlow<Set<String>>(emptySet())
val arrivingTripIds: StateFlow<Set<String>> = _arrivingTripIds.asStateFlow()
```

Add `setUserLocation`, `toggleArrivingFilter`:

```kotlin
fun setUserLocation(lat: Double, lng: Double) {
    _userLocation.value = Pair(lat, lng)
    if (_arrivingFilterEnabled.value) refreshArrivingFilter()
}

fun toggleArrivingFilter(): Boolean {
    val loc = _userLocation.value ?: return false // returns false = no location
    _arrivingFilterEnabled.value = !_arrivingFilterEnabled.value
    if (_arrivingFilterEnabled.value) refreshArrivingFilter()
    else _arrivingTripIds.value = emptySet()
    return true
}

private fun refreshArrivingFilter() {
    val loc = _userLocation.value ?: return
    viewModelScope.launch {
        val cal = java.util.Calendar.getInstance()
        val nowStr = "%02d:%02d:%02d".format(
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE),
            cal.get(java.util.Calendar.SECOND)
        )
        cal.add(java.util.Calendar.MINUTE, 30)
        val plusStr = "%02d:%02d:%02d".format(
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE),
            cal.get(java.util.Calendar.SECOND)
        )
        _arrivingTripIds.value = transitRepo.getActiveTripIdsNear(
            loc.first, loc.second, 500.0, nowStr, plusStr
        )
    }
}
```

- [ ] **Step 4: Apply arriving filter to vehicles in MapViewModel**

The `vehicles` StateFlow already returns all enriched vehicles. The filter is applied at the display level in MapScreen (same pattern as `directionTripIds`). No changes to `vehicles` StateFlow — keep it clean.

- [ ] **Step 5: Update MapScreen to collect location + show filter chip**

In `MapScreen.kt`, add imports:
```kotlin
import androidx.compose.material3.FilterChip
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
```

Collect new VM states:
```kotlin
val arrivingFilterEnabled by vm.arrivingFilterEnabled.collectAsStateWithLifecycle()
val arrivingTripIds by vm.arrivingTripIds.collectAsStateWithLifecycle()
```

After the `LaunchedEffect(Unit)` that requests location permission, add a location-feed effect:
```kotlin
LaunchedEffect(locationEnabled) {
    if (!locationEnabled) return@LaunchedEffect
    try {
        @Suppress("MissingPermission")
        val loc = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
        if (loc != null) vm.setUserLocation(loc.latitude, loc.longitude)
    } catch (_: Exception) {}
}
```

Update the vehicle visibility filter inside `GoogleMap {}` (replace the existing `val visible = when {...}` block):

```kotlin
val visible = when {
    arrivingFilterEnabled && arrivingTripIds.isNotEmpty() ->
        vehicle.tripId != null && vehicle.tripId in arrivingTripIds
    arrivingFilterEnabled && arrivingTripIds.isEmpty() -> false
    currentFocus == null -> true
    directionTripIds.isNotEmpty() ->
        vehicle.routeId == currentFocus &&
        (vehicle.tripId == null || vehicle.tripId in directionTripIds)
    else -> vehicle.routeId == currentFocus
}
```

Add the "Arriving near me" `FilterChip` as a floating overlay. Place it inside the outer `Box(modifier = Modifier.fillMaxSize())`, AFTER the `currentFocus?.let { ... FocusedRouteBanner }` block:

```kotlin
if (currentFocus == null) {
    FilterChip(
        selected = arrivingFilterEnabled,
        onClick = {
            val ok = vm.toggleArrivingFilter()
            if (!ok) { /* location not available — snackbar would need SnackbarHostState here */ }
        },
        label = { Text("Arriving near me") },
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 12.dp, bottom = 16.dp)
    )
}
```

- [ ] **Step 6: Build and verify**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

---

## Task 6: F2a — Direct Trip Query

**Files:**
- Create: `app/src/main/java/com/example/bt_transit/data/local/projection/DirectTripResult.kt`
- Modify: `app/src/main/java/com/example/bt_transit/data/local/dao/StopTimeDao.kt`
- Modify: `app/src/main/java/com/example/bt_transit/data/repository/TransitRepository.kt`

- [ ] **Step 1: Create DirectTripResult projection**

```kotlin
// app/src/main/java/com/example/bt_transit/data/local/projection/DirectTripResult.kt
package com.example.bt_transit.data.local.projection

data class DirectTripResult(
    val tripId: String,
    val fromDepartureTime: String,
    val toArrivalTime: String
)
```

- [ ] **Step 2: Add `findDirectTrip` query to StopTimeDao**

Append to `StopTimeDao.kt`:

```kotlin
@Query("""
    SELECT st1.tripId AS tripId,
           st1.departureTime AS fromDepartureTime,
           st2.arrivalTime AS toArrivalTime
    FROM stop_times st1
    INNER JOIN stop_times st2 ON st1.tripId = st2.tripId
    WHERE st1.stopId = :fromStopId
      AND st2.stopId = :toStopId
      AND st1.stopSequence < st2.stopSequence
      AND st1.departureTime >= :afterTime
    ORDER BY st1.departureTime ASC
    LIMIT 1
""")
suspend fun findDirectTrip(
    fromStopId: String,
    toStopId: String,
    afterTime: String
): DirectTripResult?
```

Add the import at the top of `StopTimeDao.kt`:
```kotlin
import com.example.bt_transit.data.local.projection.DirectTripResult
```

- [ ] **Step 3: Add `findDirectTrip` to TransitRepository**

Append to `TransitRepository.kt`:

```kotlin
/**
 * Finds the earliest direct trip from [fromStopId] to [toStopId] departing at or after [afterTime].
 * Returns null if no single trip serves both stops in sequence.
 */
suspend fun findDirectTrip(
    fromStopId: String,
    toStopId: String,
    afterTime: String
): com.example.bt_transit.data.local.projection.DirectTripResult? =
    db.stopTimeDao().findDirectTrip(fromStopId, toStopId, afterTime)
```

- [ ] **Step 4: Build to verify Room accepts the query**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL. Room validates SQL at compile time — any SQL errors surface here.

---

## Task 7: F2b — Arrival Planner UI

**Files:**
- Modify: `app/src/main/java/com/example/bt_transit/ui/schedule/ScheduleViewModel.kt`
- Modify: `app/src/main/java/com/example/bt_transit/ui/schedule/ScheduleScreen.kt`

- [ ] **Step 1: Add planner state to ScheduleViewModel**

In `ScheduleViewModel.kt`, add these imports:
```kotlin
import com.example.bt_transit.domain.model.Stop
import com.example.bt_transit.data.local.projection.DirectTripResult
```

Add planner state after the existing fields:

```kotlin
data class PlannerResult(
    val fromStop: Stop,
    val toStop: Stop,
    val departureTime: String,
    val arrivalTime: String,
    val durationMin: Int
)

private val _plannerFrom = MutableStateFlow<Stop?>(null)
val plannerFrom: StateFlow<Stop?> = _plannerFrom.asStateFlow()

private val _plannerTo = MutableStateFlow<Stop?>(null)
val plannerTo: StateFlow<Stop?> = _plannerTo.asStateFlow()

private val _plannerResult = MutableStateFlow<PlannerResult?>(null)
val plannerResult: StateFlow<PlannerResult?> = _plannerResult.asStateFlow()

private val _plannerLoading = MutableStateFlow(false)
val plannerLoading: StateFlow<Boolean> = _plannerLoading.asStateFlow()

private val _stopSearchResults = MutableStateFlow<List<Stop>>(emptyList())
val stopSearchResults: StateFlow<List<Stop>> = _stopSearchResults.asStateFlow()
```

Add search and planner methods:

```kotlin
fun searchStopsFor(query: String) {
    if (query.length < 2) { _stopSearchResults.value = emptyList(); return }
    viewModelScope.launch {
        _stopSearchResults.value = transitRepository.searchStops(query)
    }
}

fun setFromStop(stop: Stop) {
    _plannerFrom.value = stop
    _stopSearchResults.value = emptyList()
    runPlannerIfReady()
}

fun setToStop(stop: Stop) {
    _plannerTo.value = stop
    _stopSearchResults.value = emptyList()
    runPlannerIfReady()
}

fun clearPlanner() {
    _plannerFrom.value = null
    _plannerTo.value = null
    _plannerResult.value = null
    _stopSearchResults.value = emptyList()
}

private fun runPlannerIfReady() {
    val from = _plannerFrom.value ?: return
    val to = _plannerTo.value ?: return
    _plannerLoading.value = true
    _plannerResult.value = null
    viewModelScope.launch {
        val result = transitRepository.findDirectTrip(from.stopId, to.stopId, currentTimeStr())
        _plannerResult.value = if (result != null) {
            PlannerResult(
                fromStop = from,
                toStop = to,
                departureTime = result.fromDepartureTime,
                arrivalTime = result.toArrivalTime,
                durationMin = minutesBetween(result.fromDepartureTime, result.toArrivalTime)
            )
        } else null
        _plannerLoading.value = false
    }
}

private fun minutesBetween(from: String, to: String): Int {
    fun toMin(t: String): Int {
        val parts = t.split(":")
        return (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }
    return (toMin(to) - toMin(from)).coerceAtLeast(0)
}
```

- [ ] **Step 2: Add ArrivalPlannerSection composable to ScheduleScreen.kt**

In `ScheduleScreen.kt`, add imports:
```kotlin
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import com.example.bt_transit.domain.model.Stop
import com.example.bt_transit.ui.schedule.PlannerResult
```

Add the planner section composable:

```kotlin
@Composable
private fun ArrivalPlannerSection(
    fromStop: Stop?,
    toStop: Stop?,
    result: PlannerResult?,
    isLoading: Boolean,
    searchResults: List<Stop>,
    onSearch: (String) -> Unit,
    onFromSelected: (Stop) -> Unit,
    onToSelected: (Stop) -> Unit,
    onClear: () -> Unit
) {
    var expandedPlanner by remember { mutableStateOf(false) }
    var pickingFrom by remember { mutableStateOf(false) }
    var pickingTo by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedPlanner = !expandedPlanner }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Arrival Planner",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expandedPlanner) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            AnimatedVisibility(visible = expandedPlanner, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickingFrom = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(8.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = fromStop?.name ?: "From stop…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (fromStop == null)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterHorizontally).size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickingTo = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(8.dp).clip(CircleShape)
                                    .background(Color(0xFF00BFA5))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = toStop?.name ?: "To stop…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (toStop == null)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    when {
                        isLoading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp),
                            strokeWidth = 2.dp
                        )
                        result != null -> PlannerResultCard(result)
                        fromStop != null && toStop != null -> Text(
                            text = "No direct route — try searching nearby stops",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }

                    if (fromStop != null || toStop != null) {
                        TextButton(onClick = onClear) { Text("Clear") }
                    }
                }
            }
        }
    }

    if (pickingFrom || pickingTo) {
        Dialog(onDismissRequest = { pickingFrom = false; pickingTo = false; searchQuery = "" }) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = if (pickingFrom) "Pick From Stop" else "Pick To Stop",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { q -> searchQuery = q; onSearch(q) },
                        label = { Text("Stop name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                        items(searchResults, key = { it.stopId }) { stop ->
                            Text(
                                text = stop.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (pickingFrom) onFromSelected(stop) else onToSelected(stop)
                                        pickingFrom = false; pickingTo = false; searchQuery = ""
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerResultCard(result: PlannerResult) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = primary.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Depart ${formatGtfsTime(result.departureTime)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = primary
                )
                Text(
                    text = "Arrive ${formatGtfsTime(result.arrivalTime)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Text(
                text = "${result.durationMin} min",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }
    }
}
```

- [ ] **Step 3: Wire ArrivalPlannerSection into ScheduleScreen**

In `ScheduleScreen`, collect the planner states:
```kotlin
val plannerFrom by vm.plannerFrom.collectAsStateWithLifecycle()
val plannerTo by vm.plannerTo.collectAsStateWithLifecycle()
val plannerResult by vm.plannerResult.collectAsStateWithLifecycle()
val plannerLoading by vm.plannerLoading.collectAsStateWithLifecycle()
val stopSearchResults by vm.stopSearchResults.collectAsStateWithLifecycle()
```

Place `ArrivalPlannerSection` as the first item inside the `LazyColumn`:
```kotlin
LazyColumn(...) {
    item {
        ArrivalPlannerSection(
            fromStop = plannerFrom,
            toStop = plannerTo,
            result = plannerResult,
            isLoading = plannerLoading,
            searchResults = stopSearchResults,
            onSearch = { vm.searchStopsFor(it) },
            onFromSelected = { vm.setFromStop(it) },
            onToSelected = { vm.setToStop(it) },
            onClear = { vm.clearPlanner() }
        )
    }
    items(routes, key = { it.routeId }) { route -> ... }
}
```

Also add `import androidx.compose.foundation.layout.fillMaxHeight` and `import androidx.compose.ui.window.Dialog` and `import androidx.compose.material.icons.filled.SwapVert` to `ScheduleScreen.kt`.

- [ ] **Step 4: Build and verify**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

---

## Task 8: F7a — Reminder Infrastructure

**Files:**
- Create: `app/src/main/java/com/example/bt_transit/notifications/ReminderScheduler.kt`
- Create: `app/src/main/java/com/example/bt_transit/notifications/ReminderReceiver.kt`
- Create: `app/src/main/java/com/example/bt_transit/notifications/BootReceiver.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create ReminderScheduler.kt**

```kotlin
// app/src/main/java/com/example/bt_transit/notifications/ReminderScheduler.kt
package com.example.bt_transit.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject

private const val PREFS_NAME = "bt_reminders"
private const val KEY_REMINDERS = "reminders"

data class ReminderInfo(
    val key: String,            // unique: routeShortName + departureTime + stopName
    val routeShortName: String,
    val stopName: String,
    val fireAtMs: Long          // epoch ms when alarm fires (departure - 5 min)
)

fun scheduleReminder(context: Context, info: ReminderInfo) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = ReminderReceiver.buildIntent(context, info)
    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, info.fireAtMs, intent)
    saveReminder(context, info)
}

fun cancelReminder(context: Context, key: String) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = ReminderReceiver.buildIntent(context, ReminderInfo(key, "", "", 0L))
    am.cancel(intent)
    removeReminder(context, key)
}

fun isReminderScheduled(context: Context, key: String): Boolean =
    loadReminders(context).any { it.key == key }

fun rescheduleAllAfterBoot(context: Context) {
    val now = System.currentTimeMillis()
    loadReminders(context)
        .filter { it.fireAtMs > now }
        .forEach { scheduleReminder(context, it) }
}

internal fun saveReminder(context: Context, info: ReminderInfo) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = loadReminders(context).toMutableList()
    existing.removeAll { it.key == info.key }
    existing.add(info)
    val arr = JSONArray()
    existing.forEach { r ->
        arr.put(JSONObject().apply {
            put("key", r.key)
            put("route", r.routeShortName)
            put("stop", r.stopName)
            put("fireAt", r.fireAtMs)
        })
    }
    prefs.edit().putString(KEY_REMINDERS, arr.toString()).apply()
}

internal fun removeReminder(context: Context, key: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val updated = loadReminders(context).filter { it.key != key }
    val arr = JSONArray()
    updated.forEach { r ->
        arr.put(JSONObject().apply {
            put("key", r.key)
            put("route", r.routeShortName)
            put("stop", r.stopName)
            put("fireAt", r.fireAtMs)
        })
    }
    prefs.edit().putString(KEY_REMINDERS, arr.toString()).apply()
}

fun loadReminders(context: Context): List<ReminderInfo> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = prefs.getString(KEY_REMINDERS, "[]") ?: "[]"
    val arr = try { JSONArray(json) } catch (_: Exception) { return emptyList() }
    return (0 until arr.length()).mapNotNull { i ->
        val obj = arr.optJSONObject(i) ?: return@mapNotNull null
        ReminderInfo(
            key = obj.optString("key"),
            routeShortName = obj.optString("route"),
            stopName = obj.optString("stop"),
            fireAtMs = obj.optLong("fireAt")
        )
    }
}
```

- [ ] **Step 2: Create ReminderReceiver.kt**

```kotlin
// app/src/main/java/com/example/bt_transit/notifications/ReminderReceiver.kt
package com.example.bt_transit.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bt_transit.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val route = intent.getStringExtra(EXTRA_ROUTE) ?: return
        val stop = intent.getStringExtra(EXTRA_STOP) ?: return
        val key = intent.getStringExtra(EXTRA_KEY) ?: return

        val notification = NotificationCompat.Builder(context, TransitNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bus_notification)
            .setContentTitle("Departure in 5 min")
            .setContentText("Route $route leaves from $stop soon")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(key.hashCode(), notification)
        } catch (_: SecurityException) {}

        removeReminder(context, key) // clean up after firing
    }

    companion object {
        private const val EXTRA_ROUTE = "route"
        private const val EXTRA_STOP = "stop"
        private const val EXTRA_KEY = "key"
        private const val REQUEST_CODE_BASE = 0x4200

        fun buildIntent(context: Context, info: ReminderInfo): PendingIntent {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_ROUTE, info.routeShortName)
                putExtra(EXTRA_STOP, info.stopName)
                putExtra(EXTRA_KEY, info.key)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
            return PendingIntent.getBroadcast(
                context,
                info.key.hashCode(),
                intent,
                flags
            )
        }
    }
}
```

- [ ] **Step 3: Create BootReceiver.kt**

```kotlin
// app/src/main/java/com/example/bt_transit/notifications/BootReceiver.kt
package com.example.bt_transit.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllAfterBoot(context)
        }
    }
}
```

- [ ] **Step 4: Update AndroidManifest.xml for receivers + permission**

Add `RECEIVE_BOOT_COMPLETED` permission after the other `<uses-permission>` lines:
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
```

Add both receivers inside `<application>`:
```xml
<receiver android:name=".notifications.ReminderReceiver" android:exported="false"/>
<receiver android:name=".notifications.BootReceiver" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
    </intent-filter>
</receiver>
```

- [ ] **Step 5: Build and verify**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

---

## Task 9: F7b — Reminder UI in Schedule Screen

**Files:**
- Modify: `app/src/main/java/com/example/bt_transit/ui/schedule/ScheduleViewModel.kt`
- Modify: `app/src/main/java/com/example/bt_transit/ui/schedule/ScheduleScreen.kt`

- [ ] **Step 1: Add reminder state to ScheduleViewModel**

In `ScheduleViewModel.kt`, add imports:
```kotlin
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.bt_transit.notifications.ReminderInfo
import com.example.bt_transit.notifications.ReminderScheduler
import com.example.bt_transit.notifications.cancelReminder
import com.example.bt_transit.notifications.isReminderScheduled
import com.example.bt_transit.notifications.loadReminders
import com.example.bt_transit.notifications.scheduleReminder
```

Update the constructor to inject context:
```kotlin
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val transitRepository: TransitRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
```

Add reminder state:
```kotlin
private val _scheduledReminders = MutableStateFlow(
    loadReminders(context).map { it.key }.toSet()
)
val scheduledReminders: StateFlow<Set<String>> = _scheduledReminders.asStateFlow()
```

Add `reminderKey` and `toggleReminder` methods:
```kotlin
fun reminderKey(routeShortName: String, departureTime: String, stopName: String): String =
    "$routeShortName|$departureTime|$stopName"

fun toggleReminder(
    routeShortName: String,
    departureTime: String,
    stopName: String
) {
    val key = reminderKey(routeShortName, departureTime, stopName)
    if (isReminderScheduled(context, key)) {
        cancelReminder(context, key)
        _scheduledReminders.value = _scheduledReminders.value - key
    } else {
        val fireAtMs = departureTimeToEpochMs(departureTime) - 5 * 60 * 1000L
        if (fireAtMs <= System.currentTimeMillis()) return // departure already passed
        val info = ReminderInfo(key, routeShortName, stopName, fireAtMs)
        scheduleReminder(context, info)
        _scheduledReminders.value = _scheduledReminders.value + key
    }
}

private fun departureTimeToEpochMs(timeStr: String): Long {
    val parts = timeStr.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: return 0L
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val cal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, h % 24)
        set(java.util.Calendar.MINUTE, m)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
        if (h >= 24) add(java.util.Calendar.DAY_OF_YEAR, 1)
    }
    return cal.timeInMillis
}
```

- [ ] **Step 2: Update DepartureChip to show bell icon**

In `ScheduleScreen.kt`, add imports:
```kotlin
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
```

Update `RouteRow` to pass `scheduledReminders`, `routeShortName`, and `onReminderToggle`:

Update the signature of `RouteRow`:
```kotlin
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RouteRow(
    route: Route,
    departureTimes: List<String>?,
    scheduledReminders: Set<String>,
    onExpand: () -> Unit,
    onDepartureClick: (String) -> Unit,
    onReminderToggle: (String) -> Unit
)
```

Inside `RouteRow`, in the `else ->` branch, update the `FlowRow` to use `DepartureChipWithBell`:
```kotlin
FlowRow(
    modifier = Modifier.padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    departureTimes.forEach { timeStr ->
        val key = "${route.shortName}|$timeStr|"
        DepartureChipWithBell(
            timeStr = timeStr,
            routeColor = Color(route.color),
            hasReminder = scheduledReminders.any { it.startsWith("${route.shortName}|$timeStr|") },
            onClick = { onDepartureClick(timeStr) },
            onBellClick = { onReminderToggle(timeStr) }
        )
    }
}
```

Add `DepartureChipWithBell` composable (replaces `DepartureChip`):
```kotlin
@Composable
private fun DepartureChipWithBell(
    timeStr: String,
    routeColor: Color,
    hasReminder: Boolean,
    onClick: () -> Unit,
    onBellClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = routeColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(modifier = Modifier.clickable { onClick() }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = routeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = formatGtfsTime(timeStr),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = routeColor
                    )
                }
            }
            IconButton(
                onClick = onBellClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (hasReminder) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                    contentDescription = if (hasReminder) "Cancel reminder" else "Set reminder",
                    tint = if (hasReminder) routeColor else routeColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
```

- [ ] **Step 3: Wire up in ScheduleScreen**

Collect `scheduledReminders`:
```kotlin
val scheduledReminders by vm.scheduledReminders.collectAsStateWithLifecycle()
```

Update the `RouteRow` call:
```kotlin
RouteRow(
    route = route,
    departureTimes = departures["${route.routeId}:$selectedDay"],
    scheduledReminders = scheduledReminders,
    onExpand = { vm.loadDeparturesFor(route.routeId) },
    onDepartureClick = { timeStr -> vm.openTripDetail(route, timeStr) },
    onReminderToggle = { timeStr ->
        vm.toggleReminder(route.shortName, timeStr, "")
    }
)
```

- [ ] **Step 4: Build and verify**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

---

## Task 10: F3a — Trip Ratings DB

**Files:**
- Create: `app/src/main/java/com/example/bt_transit/data/local/entity/RatingEntity.kt`
- Create: `app/src/main/java/com/example/bt_transit/data/local/dao/RatingDao.kt`
- Modify: `app/src/main/java/com/example/bt_transit/data/local/BTDatabase.kt`
- Modify: `app/src/main/java/com/example/bt_transit/di/DatabaseModule.kt`

- [ ] **Step 1: Create RatingEntity.kt**

```kotlin
// app/src/main/java/com/example/bt_transit/data/local/entity/RatingEntity.kt
package com.example.bt_transit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: String?,
    val routeId: String?,
    val stars: Int,
    val comment: String = "",
    val timestamp: Long
)
```

- [ ] **Step 2: Create RatingDao.kt**

```kotlin
// app/src/main/java/com/example/bt_transit/data/local/dao/RatingDao.kt
package com.example.bt_transit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bt_transit.data.local.entity.RatingEntity

@Dao
interface RatingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rating: RatingEntity)

    @Query("SELECT AVG(stars) FROM ratings WHERE routeId = :routeId")
    suspend fun getAverageForRoute(routeId: String): Float?

    @Query("SELECT * FROM ratings ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<RatingEntity>
}
```

- [ ] **Step 3: Update BTDatabase to version 3**

Replace the entire `BTDatabase.kt` with:

```kotlin
package com.example.bt_transit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bt_transit.data.local.dao.RatingDao
import com.example.bt_transit.data.local.dao.RouteDao
import com.example.bt_transit.data.local.dao.ShapeDao
import com.example.bt_transit.data.local.dao.StopDao
import com.example.bt_transit.data.local.dao.StopTimeDao
import com.example.bt_transit.data.local.dao.TripDao
import com.example.bt_transit.data.local.dao.WaypointDao
import com.example.bt_transit.data.local.entity.RatingEntity
import com.example.bt_transit.data.local.entity.RouteEntity
import com.example.bt_transit.data.local.entity.ShapeEntity
import com.example.bt_transit.data.local.entity.StopEntity
import com.example.bt_transit.data.local.entity.StopTimeEntity
import com.example.bt_transit.data.local.entity.TripEntity
import com.example.bt_transit.data.local.entity.WaypointEntity

@Database(
    entities = [
        StopEntity::class,
        RouteEntity::class,
        TripEntity::class,
        StopTimeEntity::class,
        ShapeEntity::class,
        WaypointEntity::class,
        RatingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BTDatabase : RoomDatabase() {
    abstract fun stopDao(): StopDao
    abstract fun routeDao(): RouteDao
    abstract fun tripDao(): TripDao
    abstract fun stopTimeDao(): StopTimeDao
    abstract fun shapeDao(): ShapeDao
    abstract fun waypointDao(): WaypointDao
    abstract fun ratingDao(): RatingDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `ratings` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `tripId` TEXT,
                        `routeId` TEXT,
                        `stars` INTEGER NOT NULL,
                        `comment` TEXT NOT NULL DEFAULT '',
                        `timestamp` INTEGER NOT NULL
                    )"""
                )
            }
        }
    }
}
```

- [ ] **Step 4: Update DatabaseModule to use migration**

Replace `DatabaseModule.kt` with:

```kotlin
package com.example.bt_transit.di

import android.content.Context
import androidx.room.Room
import com.example.bt_transit.data.local.BTDatabase
import com.example.bt_transit.data.local.dao.RatingDao
import com.example.bt_transit.data.local.dao.RouteDao
import com.example.bt_transit.data.local.dao.ShapeDao
import com.example.bt_transit.data.local.dao.StopDao
import com.example.bt_transit.data.local.dao.StopTimeDao
import com.example.bt_transit.data.local.dao.TripDao
import com.example.bt_transit.data.local.dao.WaypointDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): BTDatabase =
        Room.databaseBuilder(ctx, BTDatabase::class.java, "bt_transit.db")
            .addMigrations(BTDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration() // handles anything older than v2
            .build()

    @Provides fun provideStopDao(db: BTDatabase): StopDao = db.stopDao()
    @Provides fun provideRouteDao(db: BTDatabase): RouteDao = db.routeDao()
    @Provides fun provideTripDao(db: BTDatabase): TripDao = db.tripDao()
    @Provides fun provideStopTimeDao(db: BTDatabase): StopTimeDao = db.stopTimeDao()
    @Provides fun provideShapeDao(db: BTDatabase): ShapeDao = db.shapeDao()
    @Provides fun provideWaypointDao(db: BTDatabase): WaypointDao = db.waypointDao()
    @Provides fun provideRatingDao(db: BTDatabase): RatingDao = db.ratingDao()
}
```

- [ ] **Step 5: Build and verify**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL. Room code-gen will validate the migration SQL at compile time.

---

## Task 11: F3b — Trip Ratings UI

**Files:**
- Create: `app/src/main/java/com/example/bt_transit/data/repository/RatingRepository.kt`
- Modify: `app/src/main/java/com/example/bt_transit/ui/map/MapViewModel.kt`
- Modify: `app/src/main/java/com/example/bt_transit/ui/map/MapScreen.kt`

- [ ] **Step 1: Create RatingRepository.kt**

```kotlin
// app/src/main/java/com/example/bt_transit/data/repository/RatingRepository.kt
package com.example.bt_transit.data.repository

import com.example.bt_transit.data.local.dao.RatingDao
import com.example.bt_transit.data.local.entity.RatingEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepository @Inject constructor(private val ratingDao: RatingDao) {

    suspend fun submitRating(tripId: String?, routeId: String?, stars: Int, comment: String) {
        ratingDao.insert(
            RatingEntity(
                tripId = tripId,
                routeId = routeId,
                stars = stars,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getAverageForRoute(routeId: String): Float? =
        ratingDao.getAverageForRoute(routeId)
}
```

- [ ] **Step 2: Add onBoard + rating state to MapViewModel**

In `MapViewModel.kt`, inject `RatingRepository` and add new state:

```kotlin
@HiltViewModel
class MapViewModel @Inject constructor(
    private val transitRepo: TransitRepository,
    private val realtimeRepo: RealtimeRepository,
    private val ratingRepo: RatingRepository
) : ViewModel() {
```

Add after `_selectedStop`:
```kotlin
private val _onBoardTripId = MutableStateFlow<String?>(null)
val onBoardTripId: StateFlow<String?> = _onBoardTripId.asStateFlow()

private val _ratingSubmitted = MutableStateFlow(false)
val ratingSubmitted: StateFlow<Boolean> = _ratingSubmitted.asStateFlow()
```

Add methods:
```kotlin
fun boardBus(tripId: String) {
    _onBoardTripId.value = tripId
    _ratingSubmitted.value = false
}

fun alightBus() {
    _onBoardTripId.value = null
    _ratingSubmitted.value = false
}

fun submitRating(tripId: String?, routeId: String?, stars: Int, comment: String) {
    viewModelScope.launch {
        ratingRepo.submitRating(tripId, routeId, stars, comment)
        _ratingSubmitted.value = true
    }
}
```

Also update `dismissBusSheet` to clear board state if the user wasn't on this bus:
```kotlin
fun dismissBusSheet() {
    _selectedBus.value = null
}
```

- [ ] **Step 3: Add "I'm on this bus" + rating UI to bus sheet in MapScreen.kt**

In `MapScreen.kt`, add imports:
```kotlin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableIntStateOf
```

Collect `onBoardTripId` and `ratingSubmitted` states:
```kotlin
val onBoardTripId by vm.onBoardTripId.collectAsStateWithLifecycle()
val ratingSubmitted by vm.ratingSubmitted.collectAsStateWithLifecycle()
```

Inside the bus detail sheet `ModalBottomSheet` content, after `Spacer(Modifier.height(24.dp))` at the end, add the boarding/rating section:

```kotlin
val isOnBoard = info.vehicle.tripId != null && onBoardTripId == info.vehicle.tripId
val routeId = info.route?.routeId

if (!isOnBoard && !ratingSubmitted) {
    OutlinedButton(
        onClick = { info.vehicle.tripId?.let { vm.boardBus(it) } },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("I'm on this bus")
    }
} else if (isOnBoard && !ratingSubmitted) {
    var selectedStars by remember { mutableIntStateOf(0) }
    var commentText by remember { mutableStateOf("") }

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    Text(
        text = "Rate this trip",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { star ->
            IconButton(onClick = { selectedStars = star }, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$star stars",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = commentText,
        onValueChange = { commentText = it },
        label = { Text("Comment (optional)") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 2
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { vm.alightBus() }, modifier = Modifier.weight(1f)) {
            Text("Cancel")
        }
        Button(
            onClick = {
                if (selectedStars > 0) {
                    vm.submitRating(info.vehicle.tripId, routeId, selectedStars, commentText)
                }
            },
            enabled = selectedStars > 0,
            modifier = Modifier.weight(1f)
        ) {
            Text("Submit")
        }
    }
} else if (ratingSubmitted) {
    Text(
        text = "Thanks for your feedback!",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
Spacer(Modifier.height(16.dp))
```

Add `import androidx.compose.runtime.mutableStateOf` if not already present.

- [ ] **Step 4: Build and verify**

Run: `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

---

## Self-Review Checklist

**Spec coverage:**

| Spec Requirement | Task |
|-----------------|------|
| F6: UMO redirect with verified package name warning | Task 1 ✓ |
| F6: `<queries>` block in manifest | Task 1 ✓ |
| F6: Play Store fallback (not `market://`) | Task 1 ✓ |
| F5: Today/Tomorrow chips | Task 2 ✓ |
| F5: Cache invalidation on day change | Task 2 ✓ |
| F5: Holiday caveat label | Task 2 ✓ |
| F4: FavoritesRepository, max 3, SharedPreferences | Task 3 ✓ |
| F4: Favorites float to top in HomeScreen | Task 4 ✓ |
| F4: Snackbar when at max capacity | Task 4 ✓ |
| F4: "Focused direction" = `_selectedDirection` | Task 5 ✓ |
| F4: Arriving filter ≤500m, 30min window | Task 5 ✓ |
| F4: Filter chip only in full-map view | Task 5 ✓ |
| F2: New DAO query with SQL written out | Task 6 ✓ |
| F2: DirectTripResult projection | Task 6 ✓ |
| F2: ArrivalPlannerSection collapsible | Task 7 ✓ |
| F2: Stop picker dialog with search | Task 7 ✓ |
| F2: "No direct route" copy | Task 7 ✓ |
| F7: `setAndAllowWhileIdle` not `setExactAndAllowWhileIdle` | Task 8 ✓ |
| F7: BOOT_COMPLETED receiver | Task 8 ✓ |
| F7: Cancellation + dedupe via SharedPreferences | Task 8/9 ✓ |
| F7: Bell icon on departure chips | Task 9 ✓ |
| F3: "Trip ratings" not "driver ratings" | Task 10/11 ✓ |
| F3: Explicit "I'm on this bus" boarding gate | Task 11 ✓ |
| F3: Room v2→v3 migration + `fallbackToDestructiveMigration` kept for older versions | Task 10 ✓ |
| F3: RatingRepository | Task 11 ✓ |
