package com.zincstate.hepta.presentation.about

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            val edgeThreshold = 40.dp.toPx()
            var isFromEdge = false
            detectHorizontalDragGestures(
                onDragStart = { offset ->
                    isFromEdge = offset.x < edgeThreshold
                },
                onDragEnd = {
                    isFromEdge = false
                },
                onDragCancel = {
                    isFromEdge = false
                },
                onHorizontalDrag = { change, dragAmount ->
                    if (isFromEdge && dragAmount > 20f) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                        change.consume()
                    }
                }
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PRIVACY POLICY",
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            item {
                Text(
                    text = "HEPTA Privacy Policy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Effective Date: July 2026",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            item {
                PolicySection(
                    title = "Data Collection & Storage",
                    body = "HEPTA is designed with a privacy-first, local-first architecture. All of your tasks, notes, and milestones are stored securely on your device using a local Room Database. We do not transmit your tasks to any external servers without your explicit consent."
                )
            }

            item {
                PolicySection(
                    title = "Biometric Vault",
                    body = "The HEPTA Vault utilizes Android's native Biometric API. Your fingerprint or facial recognition data never leaves your device and is not accessible by the HEPTA app. It is strictly used to verify your identity locally."
                )
            }

            item {
                PolicySection(
                    title = "Calendar Integration",
                    body = "If you grant Calendar permissions, HEPTA reads your calendar events strictly locally to display your daily load and tags on the grid. We do not sync, store, or transmit your calendar data externally."
                )
            }

            item {
                PolicySection(
                    title = "Third-Party Services",
                    body = "Currently, HEPTA does not use any third-party analytics or tracking SDKs. Your usage data remains completely private."
                )
            }

            item {
                PolicySection(
                    title = "Your Consent",
                    body = "By using HEPTA, you consent to our local-first Privacy Policy. Since data is stored on-device, uninstalling the app or clearing its data will permanently delete your information unless you have manually exported a backup."
                )
            }
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
