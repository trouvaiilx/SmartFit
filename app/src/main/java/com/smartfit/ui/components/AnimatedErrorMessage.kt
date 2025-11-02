// FILE: app/src/main/java/com/smartfit/ui/components/AnimatedErrorMessage.kt

package com.smartfit.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedErrorMessage(
    errorMessage: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    autoDismissDelayMillis: Long = 5000L
) {
    var visible by remember(errorMessage) { mutableStateOf(false) }

    // Show animation when error appears
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            visible = true
            // Auto dismiss after delay
            delay(autoDismissDelayMillis)
            visible = false
            delay(400) // Wait for exit animation
            onDismiss()
        } else {
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible && errorMessage != null,
        enter = slideInVertically(
            animationSpec = tween(400),
            initialOffsetY = { -it }
        ) + fadeIn(animationSpec = tween(400)),
        exit = slideOutVertically(
            animationSpec = tween(400),
            targetOffsetY = { -it }
        ) + fadeOut(animationSpec = tween(400)) + shrinkVertically(animationSpec = tween(400)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        visible = false
                        // Delay dismiss to allow exit animation
                        kotlinx.coroutines.MainScope().launch {
                            delay(400)
                            onDismiss()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}