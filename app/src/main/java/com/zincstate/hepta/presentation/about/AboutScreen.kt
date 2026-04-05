package com.zincstate.hepta.presentation.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "IDENTITY", 
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
            verticalArrangement = Arrangement.spacedBy(48.dp),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            // 1. ZINCSTATE Branding
            item {
                Column {
                    Text(
                        text = "ZINCSTATE",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "by Shivansh Jasathi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Building high-performance, minimalist mobile applications for the modern professional. HEPTA is the flagship product of the Zincstate ecosystem.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // 2. The Hepta Philosophy
            item {
                SectionHeader("THE HEPTA PHILOSOPHY")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "HEPTA is designed for the high-performance 'Flow State'. It removes all cognitive load by providing a strictly monochrome, 7-day grid focused on deep work and intentional task management.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // 3. App Features
            item {
                SectionHeader("ACTIVE FEATURES")
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeatureItem("Zen 7-Day Grid", "A unified, horizontal-less scroll of your entire week.")
                    FeatureItem("Infinity Inbox", "The 'Shelf' for future intentions, tucked safely at the bottom.")
                    FeatureItem("Flow State Tracking", "Focus-session detection for pure Deep Work.")
                    FeatureItem("Weekly Analytics", "Real-time velocity and progress tracking.")
                }
            }

            // 4. Upcoming Features
            item {
                SectionHeader("UPCOMING")
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeatureItem("Biometric Lock", "Secure your intentions with fingerprint or face ID.")
                    FeatureItem("Cloud Backup", "Encrypted synchronization across all your Zincstate devices.")
                    FeatureItem("Custom Themes", "Refined monochrome variants (Graphite, Obsidian, Arctic).")
                }
            }

            // 5. The Engine (Tech Stack)
            item {
                SectionHeader("THE ENGINE")
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Kotlin", "Jetpack Compose", "Room DB (v3)", "Hilt DI", "Flow & Coroutines", "Material 3", "MVI Architecture").forEach { tech ->
                        SkillChip(tech)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    }
}

@Composable
fun FeatureItem(title: String, desc: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun ProjectItem(name: String, desc: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun SkillChip(name: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}
