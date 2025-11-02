// FILE: app/src/main/java/com/smartfit/ui/components/TypewriterText.kt

package com.smartfit.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    delayMillis: Long = 30L
) {
    var displayedText by remember(text) { mutableStateOf("") }
    var hasAnimated by remember(text) { mutableStateOf(false) }

    LaunchedEffect(text) {
        if (!hasAnimated && text.isNotEmpty()) {
            displayedText = ""

            // Split text into full Unicode characters (including emojis)
            val characters = text.codePoints()
                .toArray()
                .map { String(Character.toChars(it)) }

            for (i in characters.indices) {
                delay(delayMillis)
                displayedText = characters.take(i + 1).joinToString("")
            }

            hasAnimated = true
        } else {
            displayedText = text
        }
    }

    // Use Box with fixed width to prevent text shifting
    Box(modifier = modifier) {
        // Invisible text to reserve space
        Text(
            text = text,
            style = style,
            color = Color.Transparent,
            maxLines = 1
        )
        // Visible animated text
        Text(
            text = displayedText,
            style = style,
            color = color,
            maxLines = 1
        )
    }
}