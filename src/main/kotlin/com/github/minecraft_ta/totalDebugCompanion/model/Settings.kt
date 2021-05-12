package com.github.minecraft_ta.totalDebugCompanion.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Settings {
    var fontSize by mutableStateOf(13.sp)
    val scrollbarWidth = 13.dp

    fun lineHeight(d: Density): Dp {
        with(d) {
            return fontSize.toDp() * 1.6f
        }
    }
}
