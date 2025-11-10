// FILE: app/src/main/java/com/smartfit/ui/components/OfflineBanner.kt

package com.smartfit.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartfit.util.NetworkMonitor

@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    networkMonitor: NetworkMonitor = NetworkMonitor(LocalContext.current)
) {
    val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
    var wasOffline by remember { mutableStateOf(false) }

    // Track if we were offline to show "back online" message
    LaunchedEffect(isOnline) {
        if (!isOnline) {
            wasOffline = true
        }
    }

    AnimatedVisibility(
        visible = !isOnline || (wasOffline),
        enter = slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { -it }
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            animationSpec = tween(300),
            targetOffsetY = { -it }
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isOnline) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isOnline) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                    contentDescription = if (isOnline) "Back online" else "Offline",
                    tint = if (isOnline) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOnline) "Back Online" else "You're Offline",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isOnline) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Text(
                        text = if (isOnline) {
                            "Connection restored"
                        } else {
                            "Some features may be limited"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOnline) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
        }
    }

    // Auto-dismiss "back online" message after 3 seconds
    if (isOnline && wasOffline) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            wasOffline = false
        }
    }
}