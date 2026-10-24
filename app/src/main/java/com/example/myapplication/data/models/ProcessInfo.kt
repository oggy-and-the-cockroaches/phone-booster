package com.example.myapplication.data.models

import android.graphics.drawable.Drawable

data class ProcessInfo(
    val pid: Int,
    val name: String,
    val packageName: String,
    val memoryBytes: Long,
    val threads: Int,
    val state: Char,
    val isUserApp: Boolean,
    val icon: Drawable? = null,
    val appLabel: String? = null
)
