package com.example.bt_transit.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

private data class Place(
    val name: String,
    val address: String,
    val icon: ImageVector,
    val isWaypoint: Boolean = false
)

private val savedWaypoints = listOf(
    Place("Home", "Walnut St & 3rd St", Icons.Default.Home, isWaypoint = true),
    Place("Work", "Indiana Memorial Union", Icons.Default.Work, isWaypoint = true),
    Place("Campus", "Sample Gates, Kirkwood Ave", Icons.Default.School, isWaypoint = true)
)

private val recentSearches = listOf(
    Place("College Mall", "2815 E 3rd St, Bloomington", Icons.Default.History),
    Place("Bloomington Hospital", "601 W 2nd St, Bloomington", Icons.Default.History),
    Place("Monroe County Library", "303 E Kirkwood Ave", Icons.Default.History)
)

private val allPlaces = listOf(
    Place("Indiana University", "107 S Indiana Ave, Bloomington", Icons.Default.LocationOn),
    Place("Bloomington Transit Center", "130 W Grimes Ln", Icons.Default.LocationOn),
    Place("Sample Gates", "Kirkwood Ave & Indiana Ave", Icons.Default.LocationOn),
    Place("College Mall", "2815 E 3rd St", Icons.Default.LocationOn),
    Place("Bloomington Hospital", "601 W 2nd St", Icons.Default.LocationOn),
    Place("Monroe County Library", "303 E Kirkwood Ave", Icons.Default.LocationOn),
    Place("Eastland Plaza", "E 3rd St & Clarizz Blvd", Icons.Default.LocationOn),
    Place("Whitehall Crossing", "2850 S Creekside Dr", Icons.Default.LocationOn),
    Place("IMU", "900 E 7th St", Icons.Default.LocationOn),
    Place("IU Assembly Hall", "1001 E 17th St", Icons.Default.LocationOn),
    Place("Woodlawn Field", "1500 E 10th St", Icons.Default.LocationOn),
    Place("Curry Pike & 2nd", "Curry Pike, Bloomington", Icons.Default.LocationOn),
    Place("3rd Street Park", "750 W 3rd St", Icons.Default.LocationOn),
    Place("Switchyard Park", "1601 S Rogers St", Icons.Default.LocationOn),
    Place("North Park", "2200 N Henderson St", Icons.Default.LocationOn)
)

@Composable
fun SearchScreen(onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val results = remember(query) {
        if (query.isBlank()) emptyList()
        else allPlaces.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.address.contains(query, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onBack = onBack,
            onClear = { query = "" },
            focusRequester = focusRequester
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (query.isBlank()) {
                item {
                    SectionHeader("Saved Places")
                }
                items(savedWaypoints) { place ->
                    PlaceRow(place, onBack)
                }

                item {
                    Spacer(Modifier.height(4.dp))
                    SectionHeader("Recent Searches")
                }
                items(recentSearches) { place ->
                    PlaceRow(place, onBack)
                }
            } else if (results.isEmpty()) {
                item { NoResults(query) }
            } else {
                item {
                    SectionHeader("Results")
                }
                items(results, key = { it.name }) { place ->
                    PlaceRow(place, onBack)
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    "Search destination...",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { }),
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun PlaceRow(place: Place, onSelect: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (place.isWaypoint) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = place.icon,
                    contentDescription = null,
                    tint = if (place.isWaypoint) MaterialTheme.colorScheme.onPrimaryContainer
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (place.isWaypoint) FontWeight.Medium else FontWeight.Normal
                )
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                )
            }
        }
        Divider(
            modifier = Modifier.padding(start = 70.dp, end = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun NoResults(query: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No results for \"$query\"",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Text(
                text = "Try a stop name, street, or landmark",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
            )
        }
    }
}
