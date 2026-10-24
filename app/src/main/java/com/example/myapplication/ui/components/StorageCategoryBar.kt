package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.models.StorageOverview
import com.example.myapplication.ui.theme.ColorArchives
import com.example.myapplication.ui.theme.ColorAudio
import com.example.myapplication.ui.theme.ColorDocs
import com.example.myapplication.ui.theme.ColorImages
import com.example.myapplication.ui.theme.ColorOther
import com.example.myapplication.ui.theme.ColorVideos
import com.example.myapplication.ui.theme.DarkSurfaceBorder
import com.example.myapplication.ui.theme.TextMuted
import com.example.myapplication.ui.theme.TextPrimary
import com.example.myapplication.ui.theme.TextSecondary
import com.example.myapplication.util.FormatUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StorageCategoryBar(
    overview: StorageOverview,
    modifier: Modifier = Modifier
) {
    val total = if (overview.totalBytes > 0) overview.totalBytes.toFloat() else 1f

    val imagesFrac = (overview.imagesBytes.toFloat() / total).coerceIn(0f, 1f)
    val videosFrac = (overview.videosBytes.toFloat() / total).coerceIn(0f, 1f)
    val audioFrac = (overview.audioBytes.toFloat() / total).coerceIn(0f, 1f)
    val docsFrac = (overview.docsBytes.toFloat() / total).coerceIn(0f, 1f)
    val archivesFrac = (overview.archivesBytes.toFloat() / total).coerceIn(0f, 1f)
    val otherFrac = (overview.otherBytes.toFloat() / total).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Multi-segment horizontal bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(DarkSurfaceBorder)
        ) {
            if (imagesFrac > 0.005f) {
                Box(modifier = Modifier.weight(imagesFrac).height(14.dp).background(ColorImages))
            }
            if (videosFrac > 0.005f) {
                Box(modifier = Modifier.weight(videosFrac).height(14.dp).background(ColorVideos))
            }
            if (audioFrac > 0.005f) {
                Box(modifier = Modifier.weight(audioFrac).height(14.dp).background(ColorAudio))
            }
            if (docsFrac > 0.005f) {
                Box(modifier = Modifier.weight(docsFrac).height(14.dp).background(ColorDocs))
            }
            if (archivesFrac > 0.005f) {
                Box(modifier = Modifier.weight(archivesFrac).height(14.dp).background(ColorArchives))
            }
            if (otherFrac > 0.005f) {
                Box(modifier = Modifier.weight(otherFrac).height(14.dp).background(ColorOther))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(name = "Images", bytes = overview.imagesBytes, color = ColorImages)
            CategoryChip(name = "Videos", bytes = overview.videosBytes, color = ColorVideos)
            CategoryChip(name = "Audio", bytes = overview.audioBytes, color = ColorAudio)
            CategoryChip(name = "Docs", bytes = overview.docsBytes, color = ColorDocs)
            CategoryChip(name = "APKs/Zips", bytes = overview.archivesBytes, color = ColorArchives)
            CategoryChip(name = "Other", bytes = overview.otherBytes, color = ColorOther)
        }
    }
}

@Composable
private fun CategoryChip(
    name: String,
    bytes: Long,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = FormatUtils.formatBytes(bytes),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary
        )
    }
}
