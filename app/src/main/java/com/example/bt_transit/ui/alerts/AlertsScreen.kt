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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bt_transit.domain.model.ServiceAlert

private enum class AlertSeverity { HIGH, MEDIUM, LOW }

private fun ServiceAlert.severity(): AlertSeverity = when (effect) {
    "NO_SERVICE", "SIGNIFICANT_DELAYS" -> AlertSeverity.HIGH
    "DETOUR", "REDUCED_SERVICE", "MODIFIED_SERVICE" -> AlertSeverity.MEDIUM
    else -> AlertSeverity.LOW
}

private fun String.toDisplayLabel(): String =
    replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

@Composable
fun AlertsScreen(
    innerPadding: PaddingValues,
    vm: AlertsViewModel = viewModel(factory = AlertsViewModel.Factory)
) {
    val alerts by vm.alerts.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        AlertsHeader(activeCount = alerts.size)

        if (alerts.isEmpty()) {
            NoAlertsPlaceholder()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
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
            text = if (activeCount > 0) "$activeCount active alert${if (activeCount != 1) "s" else ""}"
                   else "All routes running normally",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun AlertCard(alert: ServiceAlert) {
    val severity = alert.severity()
    val (bgColor, iconColor, icon) = alertStyle(severity)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = alert.header.ifEmpty { alert.effect.toDisplayLabel() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (alert.description.isNotEmpty()) {
                    Text(
                        text = alert.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CauseBadge(alert.cause.toDisplayLabel())
                    Spacer(Modifier.width(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(alert.affectedRouteIds) { route ->
                            RouteBadge(route)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CauseBadge(label: String) {
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
