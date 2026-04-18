package com.example.bt_transit.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class AlertSeverity { HIGH, MEDIUM, LOW }

private data class ServiceAlert(
    val id: String,
    val cause: String,
    val effect: String,
    val header: String,
    val description: String,
    val affectedRoutes: List<String>,
    val severity: AlertSeverity,
    val timeRange: String
)

private val sampleAlerts = listOf(
    ServiceAlert(
        id = "alert_001",
        cause = "WEATHER",
        effect = "SIGNIFICANT_DELAYS",
        header = "Delays due to severe weather",
        description = "Heavy rain and flooding on 3rd Street corridor is causing delays of up to 15 minutes on Routes 1, 6, and 9. Drivers are taking alternate roads where possible.",
        affectedRoutes = listOf("1", "6", "9"),
        severity = AlertSeverity.HIGH,
        timeRange = "Until 6:00 PM today"
    ),
    ServiceAlert(
        id = "alert_002",
        cause = "CONSTRUCTION",
        effect = "DETOUR",
        header = "Route 3 detour — Dunn St construction",
        description = "Northbound Route 3 is detouring via Rogers St between 4th and 10th due to ongoing water main work. Stops on Dunn St between 4th and 10th are temporarily suspended.",
        affectedRoutes = listOf("3"),
        severity = AlertSeverity.MEDIUM,
        timeRange = "Apr 18 – Apr 25"
    ),
    ServiceAlert(
        id = "alert_003",
        cause = "MAINTENANCE",
        effect = "REDUCED_SERVICE",
        header = "Route 11 frequency reduced this weekend",
        description = "The IU Campus Loop (Route 11) will run every 30 minutes instead of every 15 minutes this Saturday and Sunday due to scheduled bus maintenance.",
        affectedRoutes = listOf("11"),
        severity = AlertSeverity.LOW,
        timeRange = "Sat–Sun, Apr 19–20"
    ),
    ServiceAlert(
        id = "alert_004",
        cause = "TECHNICAL_PROBLEM",
        effect = "MODIFIED_SERVICE",
        header = "Transit Center bay reassignments",
        description = "Routes 4, 5, and 14 are boarding from temporary bays B3 and B4 at the Transit Center while electrical work is completed on the main platform.",
        affectedRoutes = listOf("4", "5", "14"),
        severity = AlertSeverity.LOW,
        timeRange = "Today only"
    )
)

@Composable
fun AlertsScreen(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        AlertsHeader(activeCount = sampleAlerts.size)

        if (sampleAlerts.isEmpty()) {
            NoAlertsPlaceholder()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sampleAlerts, key = { it.id }) { alert ->
                    AlertCard(alert)
                }
            }
        }
    }
}

@Composable
private fun AlertsHeader(activeCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Service Alerts",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "$activeCount active alert${if (activeCount != 1) "s" else ""}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun AlertCard(alert: ServiceAlert) {
    val (bgColor, iconColor, icon) = alertStyle(alert.severity)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Severity bar + header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = alert.severity.name,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.header,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Description
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(12.dp))

                // Footer: cause badge + affected routes + time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CauseBadge(alert.cause)

                    Spacer(Modifier.width(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(alert.affectedRoutes) { route ->
                            RouteBadge(route)
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = alert.timeRange,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CauseBadge(cause: String) {
    val label = cause.replace("_", " ").lowercase()
        .replaceFirstChar { it.uppercase() }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun RouteBadge(routeNumber: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = routeNumber,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NoAlertsPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No active alerts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Text(
                text = "All routes running normally",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
            )
        }
    }
}

private data class AlertStyle(val bg: Color, val iconColor: Color, val icon: ImageVector)

@Composable
private fun alertStyle(severity: AlertSeverity): AlertStyle = when (severity) {
    AlertSeverity.HIGH -> AlertStyle(
        bg = Color(0xFFFFEBEE),
        iconColor = Color(0xFFD32F2F),
        icon = Icons.Default.Warning
    )
    AlertSeverity.MEDIUM -> AlertStyle(
        bg = Color(0xFFFFF8E1),
        iconColor = Color(0xFFF57F17),
        icon = Icons.Default.Info
    )
    AlertSeverity.LOW -> AlertStyle(
        bg = Color(0xFFE8F5E9),
        iconColor = Color(0xFF2E7D32),
        icon = Icons.Default.Build
    )
}
