package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.myapplication.data.models.ProcessInfo
import com.example.myapplication.ui.components.BoostPulseButton
import com.example.myapplication.ui.components.GaugeMeter
import com.example.myapplication.ui.components.GlassmorphismCard
import com.example.myapplication.ui.theme.CyanAccent
import com.example.myapplication.ui.theme.DarkBackground
import com.example.myapplication.ui.theme.DarkSurfaceBorder
import com.example.myapplication.ui.theme.DarkSurfaceElevated
import com.example.myapplication.ui.theme.ElectricBlue
import com.example.myapplication.ui.theme.NeonGreen
import com.example.myapplication.ui.theme.TextMuted
import com.example.myapplication.ui.theme.TextPrimary
import com.example.myapplication.ui.theme.TextSecondary
import com.example.myapplication.util.FormatUtils
import com.example.myapplication.viewmodel.RamBoosterViewModel

@Composable
fun RamBoosterScreen(
    viewModel: RamBoosterViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val ram = state.ramStats

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = 16.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RAM Booster",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Real-time kernel memory optimization",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            IconButton(onClick = { viewModel.loadData() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = CyanAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Gauge & RAM Specs Card
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GaugeMeter(
                        percent = ram.usagePercent,
                        title = "RAM USED",
                        subtitle = "${FormatUtils.formatBytes(ram.usedBytes)} of ${FormatUtils.formatBytes(ram.totalBytes)}",
                        size = 150.dp,
                        strokeWidth = 14.dp,
                        gradientColors = listOf(ElectricBlue, NeonGreen)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        RamStatPill(
                            label = "AVAILABLE",
                            value = FormatUtils.formatBytes(ram.availableBytes),
                            color = NeonGreen
                        )
                        RamStatPill(
                            label = "ACTIVE USED",
                            value = FormatUtils.formatBytes(ram.usedBytes),
                            color = ElectricBlue
                        )
                        if (ram.cachedBytes > 0) {
                            RamStatPill(
                                label = "CACHED",
                                value = FormatUtils.formatBytes(ram.cachedBytes),
                                color = CyanAccent
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Boost Pulse Button
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            BoostPulseButton(
                text = "SUPER BOOST RAM",
                isBoosting = state.isBoosting,
                onClick = { viewModel.boostRam() }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Top RAM Consumers Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BACKGROUND RAM CONSUMERS",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )
            Text(
                text = "${state.topApps.size} apps",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = CyanAccent
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of RAM consuming apps
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.topApps, key = { "${it.packageName}_${it.pid}" }) { app ->
                    RamAppItem(app = app)
                }
            }
        }
    }

    // Boost result dialog
    if (state.boostFreedBytes != null) {
        val freed = state.boostFreedBytes!!
        val count = state.killedAppsCount
        AlertDialog(
            onDismissRequest = { viewModel.dismissBoostResult() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("System Boosted!", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Released ${FormatUtils.formatBytes(freed)} of RAM by optimizing background tasks and stopping $count processes.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissBoostResult() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Great!", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
private fun RamStatPill(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

@Composable
private fun RamAppItem(app: ProcessInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (app.icon != null) {
            val bm = app.icon.toBitmap(width = 96, height = 96).asImageBitmap()
            Image(
                bitmap = bm,
                contentDescription = app.name,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceBorder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appLabel ?: app.name,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${app.packageName} • PID ${app.pid}",
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = FormatUtils.formatBytes(app.memoryBytes),
            color = NeonGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
