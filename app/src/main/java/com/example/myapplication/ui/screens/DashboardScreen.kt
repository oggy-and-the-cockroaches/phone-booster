package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.BoostPulseButton
import com.example.myapplication.ui.components.GaugeMeter
import com.example.myapplication.ui.components.GlassmorphismCard
import com.example.myapplication.ui.theme.CoralRed
import com.example.myapplication.ui.theme.CyanAccent
import com.example.myapplication.ui.theme.DarkBackground
import com.example.myapplication.ui.theme.DarkSurfaceBorder
import com.example.myapplication.ui.theme.DarkSurfaceElevated
import com.example.myapplication.ui.theme.ElectricBlue
import com.example.myapplication.ui.theme.NeonGreen
import com.example.myapplication.ui.theme.PurpleAccent
import com.example.myapplication.ui.theme.TextMuted
import com.example.myapplication.ui.theme.TextPrimary
import com.example.myapplication.ui.theme.TextSecondary
import com.example.myapplication.ui.theme.WarningAmber
import com.example.myapplication.util.FormatUtils
import com.example.myapplication.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToProcesses: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onNavigateToCache: () -> Unit,
    onNavigateToRam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val overview = state.overview

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // App Title & Native Engine Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BOOSTMAX",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "High-Performance System Booster",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Engine status chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (overview.isNativeEngineActive) NeonGreen.copy(alpha = 0.15f)
                        else CyanAccent.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = if (overview.isNativeEngineActive) NeonGreen else CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (overview.isNativeEngineActive) "C++ NDK ACTIVE" else "HYBRID ENGINE",
                        color = if (overview.isNativeEngineActive) NeonGreen else CyanAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Toast / Banner message
        AnimatedVisibility(visible = state.boostMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonGreen.copy(alpha = 0.2f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.boostMessage ?: "",
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Live Performance Gauges (RAM & CPU)
        GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "REAL-TIME HARDWARE METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    GaugeMeter(
                        percent = overview.ramStats.usagePercent,
                        title = "RAM USAGE",
                        subtitle = "${FormatUtils.formatBytes(overview.ramStats.usedBytes)} / ${FormatUtils.formatBytes(overview.ramStats.totalBytes)}",
                        gradientColors = listOf(ElectricBlue, CyanAccent)
                    )
                    GaugeMeter(
                        percent = overview.cpuUsagePercent,
                        title = "CPU LOAD",
                        subtitle = "${overview.runningProcessesCount} processes",
                        gradientColors = listOf(PurpleAccent, CoralRed)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // One-Tap Quick Boost Button
        BoostPulseButton(
            text = "QUICK BOOST SYSTEM",
            isBoosting = state.isBoosting,
            onClick = { viewModel.quickBoost() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // System Secondary Cards (Storage & Battery)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Storage Quick Card
            QuickStatCard(
                title = "STORAGE",
                value = FormatUtils.formatPercent(overview.storageUsagePercent),
                subtitle = "${FormatUtils.formatBytes(overview.freeStorageBytes)} free",
                icon = Icons.Default.Storage,
                color = WarningAmber,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToStorage
            )

            // Battery Quick Card
            QuickStatCard(
                title = "BATTERY",
                value = "${overview.batteryLevel}%",
                subtitle = "${overview.batteryTempCelsius}°C Normal",
                icon = Icons.Default.BatteryChargingFull,
                color = NeonGreen,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Navigation Quick Grid
        Text(
            text = "SYSTEM OPTIMIZATION TOOLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                title = "Process Manager",
                subtitle = "Manage & stop background tasks",
                icon = Icons.Default.Memory,
                tint = ElectricBlue,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToProcesses
            )
            ToolCard(
                title = "Storage Analyzer",
                subtitle = "Find large files & app data",
                icon = Icons.Default.Storage,
                tint = WarningAmber,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToStorage
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                title = "Cache Cleaner",
                subtitle = "Reclaim junk & temp cache",
                icon = Icons.Default.CleaningServices,
                tint = CoralRed,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToCache
            )
            ToolCard(
                title = "RAM Booster",
                subtitle = "Free memory with 1-tap boost",
                icon = Icons.Default.RocketLaunch,
                tint = NeonGreen,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToRam
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
