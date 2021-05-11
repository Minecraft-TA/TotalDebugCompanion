package com.github.minecraft_ta.totalDebugCompanion.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

object Fonts {
    @Composable
    fun jetbrainsMono() = FontFamily(
        Font(
            "jetbrainsmono_regular",
            FontWeight.Normal,
            FontStyle.Normal
        ),
        Font(
            "jetbrainsmono_italic",
            FontWeight.Normal,
            FontStyle.Italic
        ),

        Font(
            "jetbrainsmono_bold",
            FontWeight.Bold,
            FontStyle.Normal
        ),
        Font(
            "jetbrainsmono_bold_italic",
            FontWeight.Bold,
            FontStyle.Italic
        ),

        Font(
            "jetbrainsmono_extrabold",
            FontWeight.ExtraBold,
            FontStyle.Normal
        ),
        Font(
            "jetbrainsmono_extrabold_italic",
            FontWeight.ExtraBold,
            FontStyle.Italic
        ),

        Font(
            "jetbrainsmono_medium",
            FontWeight.Medium,
            FontStyle.Normal
        ),
        Font(
            "jetbrainsmono_medium_italic",
            FontWeight.Medium,
            FontStyle.Italic
        )
    )
}

fun Font(res: String, weight: FontWeight, style: FontStyle): Font =
    androidx.compose.ui.text.platform.Font("font/$res.ttf", weight, style)
