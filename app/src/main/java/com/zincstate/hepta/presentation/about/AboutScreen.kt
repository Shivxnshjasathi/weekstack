package com.zincstate.hepta.presentation.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zincstate.hepta.ui.theme.ZenTheme
import com.zincstate.hepta.ui.theme.getZenColors

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    currentTheme: ZenTheme = ZenTheme.OBSIDIAN,
    onThemeChange: (ZenTheme) -> Unit = {},
    onCustomThemeChange: (Color) -> Unit = {},
    onApplyPreset: (com.zincstate.hepta.domain.model.PresetType) -> Unit = {},
    isVaultEnabled: Boolean = false,
    onVaultToggle: (Boolean) -> Unit = {},
    lifetimeCompletedTasks: Int = 0,
    onNavigateToPrivacyPolicy: () -> Unit = {}
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var showColorPicker by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showPresetConfirm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.zincstate.hepta.domain.model.PresetType?>(null) }
    
    val sheetState = rememberModalBottomSheetState()
    
    // Premium Hepta Palette
    val premiumColors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Violet
        Color(0xFFEC4899), // Pink
        Color(0xFFF43F5E), // Rose
        Color(0xFFF59E0B), // Amber
        Color(0xFF10B981), // Emerald
        Color(0xFF06B6D4), // Cyan
        Color(0xFF3B82F6), // Blue
        Color(0xFFA8A29E), // Stone
        Color(0xFFD946EF), // Fuchsia
        Color(0xFF84cc16), // Lime
        Color(0xFFfb923c)  // Orange
    )

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
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
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
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            // 1. ZINCSTATE Branding
            item {
                Column {
                    Text(
                        text = "HEPTA",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    Text(
                        text = "by Zincstate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { uriHandler.openUri("https://www.zincstate.shop/") }
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

            // 2. Theme Selector
            item {
                SectionHeader("THEME")
                Spacer(modifier = Modifier.height(16.dp))
                val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
                val availableWidth = screenWidth - 48.dp
                val columnCount = maxOf(4, (availableWidth.value / 72).toInt())
                val spacing = 8.dp
                val itemWidth = (availableWidth - (spacing * (columnCount - 1))) / columnCount
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    // Filter out standard Dark/Light themes and the generic Custom node
                    ZenTheme.entries.filter { 
                        it != ZenTheme.OBSIDIAN && it != ZenTheme.ARCTIC && it != ZenTheme.CUSTOM && it != ZenTheme.HACKER
                    }.forEach { theme ->
                        val colors = getZenColors(theme)
                        val isLocked = theme.unlockRequirement > lifetimeCompletedTasks
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(itemWidth)
                                .clickable { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    if (isLocked) {
                                        android.widget.Toast.makeText(context, "Unlock ${theme.displayName} by completing ${theme.unlockRequirement} tasks! (Current: $lifetimeCompletedTasks/${theme.unlockRequirement})", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        onThemeChange(theme)
                                    }
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = colors.colorScheme.background,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                                    .border(
                                        width = if (currentTheme == theme) 2.dp else 1.dp,
                                        color = if (currentTheme == theme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLocked) {
                                    Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.CircleShape))
                                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = theme.displayName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentTheme == theme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontSize = 8.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    
                    // 2a. The "Prism" Custom Theme Creator Node
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(itemWidth)
                            .clickable { showColorPicker = true }
                            .padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(Color(0xFFFF0000), Color(0xFF00FF00), Color(0xFF0000FF))
                                    ),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .border(
                                    width = if (currentTheme == ZenTheme.CUSTOM) 2.dp else 1.dp,
                                    color = if (currentTheme == ZenTheme.CUSTOM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                             Icon(
                                 imageVector = Icons.Default.Add,
                                 contentDescription = null,
                                 tint = Color.White,
                                 modifier = Modifier.size(16.dp)
                             )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PRISM",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (currentTheme == ZenTheme.CUSTOM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 8.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // 2b. Goal Presets
            item {
                SectionHeader("GOAL PRESETS")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "One-tap setups for your intentional week. Presets add a curated set of tasks across all 7 days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Adaptive Grid for Presets
                val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
                val availableWidth = screenWidth - 48.dp
                val columnCount = when {
                    availableWidth < 340.dp -> 1
                    availableWidth < 600.dp -> 2
                    else -> 3
                }
                val spacing = 16.dp
                val itemWidth = (availableWidth - (spacing * (columnCount - 1))) / columnCount
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        com.zincstate.hepta.domain.model.PresetType.entries.forEach { preset ->
                            GoalPresetCard(
                                preset = preset,
                                modifier = Modifier.width(itemWidth),
                                onClick = { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    showPresetConfirm = preset 
                                }
                            )
                        }
                    }
            }

            // 2b. Vault Security Toggle
            item {
                SectionHeader("VAULT")
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BIOMETRIC LOCK",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Require fingerprint or face to open HEPTA",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    Switch(
                        checked = isVaultEnabled,
                        onCheckedChange = { onVaultToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }


            // Resources & Links
            item {
                SectionHeader("RESOURCES & LINKS")
                Spacer(modifier = Modifier.height(16.dp))
                
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri("https://weekstack-web.vercel.app/") }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Hepta Web",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Access your intentions on any device",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPrivacyPolicy() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Data collection, calendar usage & policies",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri("https://forms.gle/a9vGkDC1sx1Ur1GJ9") }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Report a Bug",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Help us improve HEPTA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri("https://forms.gle/8zKnU1T83tWmofPt7") }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Submit a Suggestion",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "We'd love to hear your ideas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Bottom Sheets & Dialogs
        if (showColorPicker) {
            ModalBottomSheet(
                onDismissRequest = { showColorPicker = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "PRISM: CHOOSE IDENTITY",
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        premiumColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color, androidx.compose.foundation.shape.CircleShape)
                                    .clickable { 
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        onCustomThemeChange(color)
                                        showColorPicker = false
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Preset Apply Confirm
        if (showPresetConfirm != null) {
            AlertDialog(
                onDismissRequest = { showPresetConfirm = null },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { 
                    Text(
                        "APPLY ${showPresetConfirm?.displayName}?", 
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 1.sp
                    ) 
                },
                text = { 
                    Text(
                        "This will populate your entire week with tasks from this preset. Existing tasks will not be deleted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ) 
                },
                confirmButton = {
                    TextButton(onClick = {
                        showPresetConfirm?.let { onApplyPreset(it) }
                        showPresetConfirm = null
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    }) {
                        Text("APPLY", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPresetConfirm = null }) {
                        Text("CANCEL", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            )
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
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun GoalPresetCard(
    preset: com.zincstate.hepta.domain.model.PresetType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDefault = preset == com.zincstate.hepta.domain.model.PresetType.DEFAULT
    Surface(
        modifier = modifier
            .clickable { onClick() },
        color = if (isDefault) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = preset.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = if (isDefault) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = preset.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                lineHeight = 14.sp
            )
        }
    }
}
