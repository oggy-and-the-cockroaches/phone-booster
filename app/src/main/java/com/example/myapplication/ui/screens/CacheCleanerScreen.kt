package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.myapplication.data.models.JunkCategory
import com.example.myapplication.data.models.JunkItemModel
import com.example.myapplication.ui.components.GlassmorphismCard
import com.example.myapplication.ui.theme.ColorArchives
import com.example.myapplication.ui.theme.ColorAudio
import com.example.myapplication.ui.theme.ColorDocs
import com.example.myapplication.ui.theme.ColorImages
import com.example.myapplication.ui.theme.CoralRed
import com.example.myapplication.ui.theme.CyanAccent
import com.example.myapplication.ui.theme.DarkBackground
import com.example.myapplication.ui.theme.DarkSurfaceElevated
import com.example.myapplication.ui.theme.NeonGreen
import com.example.myapplication.ui.theme.TextMuted
import com.example.myapplication.ui.theme.TextPrimary
import com.example.myapplication.ui.theme.TextSecondary
import com.example.myapplication.util.FormatUtils
import com.example.myapplication.viewmodel.CacheViewModel

@Composable
fun CacheCleanerScreen(
    viewModel: CacheViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val overview = state.scanOverview

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
                    text = "Cache & Junk Cleaner",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "High-speed C++ file & cache scanner",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            IconButton(onClick = { viewModel.startScan() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rescan",
                    tint = CyanAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Junk Summary Header
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RECLAIMABLE STORAGE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FormatUtils.formatBytes(overview.totalJunkBytes),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = CoralRed
                    )
                    Text(
                        text = "Selected for cleanup: ${FormatUtils.formatBytes(state.selectedTotalBytes)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Select All / Deselect All Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${overview.items.size} JUNK TARGETS",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )
            Row {
                TextButton(onClick = { viewModel.selectAll() }) {
                    Text("Select All", color = CyanAccent, fontSize = 12.sp)
                }
                TextButton(onClick = { viewModel.deselectAll() }) {
                    Text("Deselect All", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        // Junk Items List
        if (state.isScanning) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CoralRed)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Scanning cache & residual files...", color = TextSecondary, fontSize = 13.sp)
                }
            }
        } else if (overview.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your device is squeaky clean!", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("No junk files or temporary caches found.", color = TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(overview.items, key = { "${it.path}_${it.name}" }) { item ->
                    val isChecked = state.selectedItemPaths.contains(item.path)
                    JunkItemCard(
                        item = item,
                        isSelected = isChecked,
                        onToggle = { viewModel.toggleItem(item.path) }
                    )
                }
            }
        }

        // Clean Junk Button Bottom Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = { viewModel.cleanSelectedJunk() },
                enabled = !state.isCleaning && state.selectedTotalBytes > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralRed,
                    disabledContainerColor = DarkSurfaceElevated
                )
            ) {
                if (state.isCleaning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("PURGING JUNK FILES...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CLEAN ${FormatUtils.formatBytes(state.selectedTotalBytes)}",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    // Success dialog
    if (state.cleanedBytesResult != null) {
        val freed = state.cleanedBytesResult!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissResultDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clean Complete!", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Awesome! Successfully reclaimed ${FormatUtils.formatBytes(freed)} of storage space and cleared temporary junk.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissResultDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Done", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
private fun JunkItemCard(
    item: JunkItemModel,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val (icon, iconTint) = when (item.category) {
        JunkCategory.APP_CACHE -> Pair(Icons.Default.CleaningServices, CyanAccent)
        JunkCategory.TEMP_FILE -> Pair(Icons.Default.Warning, ColorAudio)
        JunkCategory.THUMBNAIL_CACHE -> Pair(Icons.Default.Image, ColorImages)
        JunkCategory.OBSOLETE_APK -> Pair(Icons.Default.FolderZip, ColorArchives)
        JunkCategory.LOG_FILE -> Pair(Icons.Default.Warning, TextMuted)
        JunkCategory.EMPTY_FOLDER -> Pair(Icons.Default.Delete, TextMuted)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = item.description,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = item.path,
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }

        Text(
            text = FormatUtils.formatBytes(item.sizeBytes),
            color = CoralRed,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.width(4.dp))

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = CoralRed,
                uncheckedColor = TextMuted
            )
        )
    }
}
