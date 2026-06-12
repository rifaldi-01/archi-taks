package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Task
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiDashboardApp(
    viewModel: ArchiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showPdfExportDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Tugas Studio", "Analitik & Jadwal", "AI Analisis PDF")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HomeRepairService,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ArchiTask Pro",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Studio Workspace",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Studio Status Indicators
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Offline Mode Indicator
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (uiState.isOnline) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (uiState.isOnline) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            ),
                            onClick = { viewModel.toggleOfflineMode() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = if (uiState.isOnline) Color(0xFF4CAF50) else Color(0xFFFF5722),
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = if (uiState.isOnline) "Cloud Sync" else "Lokal Offline",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (uiState.isOnline) MaterialTheme.colorScheme.onBackground
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // Encryption Shield Indicator
                        IconButton(
                            onClick = { viewModel.toggleE2ee() }
                        ) {
                            Icon(
                                imageVector = if (uiState.isE2eeEnabled) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = "E2EE",
                                tint = if (uiState.isE2eeEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Notification bell
                        Box {
                            IconButton(onClick = { showNotificationDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Notifikasi",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            if (uiState.notifications.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color.Red, shape = CircleShape)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = uiState.notifications.size.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    val icon = when (index) {
                        0 -> if (selectedTab == 0) Icons.Filled.ListAlt else Icons.Outlined.ListAlt
                        1 -> if (selectedTab == 1) Icons.Filled.SpaceDashboard else Icons.Outlined.SpaceDashboard
                        else -> if (selectedTab == 2) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_task_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Tugas")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Quick Status & Sync Header Bar
            SyncStatusBar(uiState = uiState, onSyncClick = { viewModel.syncCloudData() })

            // Content matching the selected Tab
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> TaskStudioTab(
                        uiState = uiState,
                        onProgressChange = { task, progress ->
                            viewModel.updateTaskProgress(task, progress)
                        },
                        onDeleteClick = { task -> viewModel.deleteTask(task) }
                    )
                    1 -> AnalyticsDashboardTab(
                        uiState = uiState,
                        onE2eeToggle = { viewModel.toggleE2ee() },
                        onChangeStorageClick = { showStorageDialog = true },
                        onPdfExportClick = { showPdfExportDialog = true }
                    )
                    2 -> AiPdfAnalyzerTab(
                        uiState = uiState,
                        onPresetSelect = { viewModel.selectDocPreset(it) },
                        onCustomContentChange = { viewModel.updateCustomPdfContent(it) },
                        onAnalyzeClick = { doc -> viewModel.analyzeDesignWithAi(doc) }
                    )
                }
            }
        }
    }

    // Task Creation Dialog Sheet
    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onTaskAdded = { title, project, designer, phase, priority, status, notes, deadline, notify, notifyMin ->
                viewModel.addTask(title, project, designer, phase, priority, status, notes, deadline, notify, notifyMin)
                showAddDialog = false
            }
        )
    }

    // Notifications Overlay
    if (showNotificationDialog) {
        NotificationsDialog(
            notifications = uiState.notifications,
            onClose = { showNotificationDialog = false },
            onClear = { viewModel.clearNotifications() }
        )
    }

    // Storage Connection Selector
    if (showStorageDialog) {
        StorageDialog(
            currentStorage = uiState.connectedStorage,
            onDismiss = { showStorageDialog = false },
            onStorageConnected = {
                viewModel.changeConnectedStorage(it)
                showStorageDialog = false
            }
        )
    }

    // PDF Report Generator Dialog (Simulator)
    if (showPdfExportDialog) {
        PdfExportDialog(
            tasks = uiState.tasks,
            onDismiss = { showPdfExportDialog = false },
            onShare = {
                viewModel.addNotification(
                    title = "Laporan Dibagikan",
                    message = "Laporan PDF Progres telah berhasil diekspor dan dibagikan ke server kolaborasi tim!"
                )
            }
        )
    }
}

// ------------------- QUICK HUD SYNC STATUS -------------------
@Composable
fun SyncStatusBar(
    uiState: ArchTaskUiState,
    onSyncClick: () -> Unit
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(uiState.isSyncing) {
        if (uiState.isSyncing) {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotation.snapTo(0f)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = "Folder",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Folder Kolaborasi Tim",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.connectedStorage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (uiState.isOnline) {
                Button(
                    onClick = onSyncClick,
                    enabled = !uiState.isSyncing,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = Color.White,
                            modifier = Modifier
                                .size(14.dp)
                                .rotate(rotation.value)
                        )
                        Text(
                            text = if (uiState.isSyncing) "Sinkron..." else "Sinkron",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline Mode",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Draft Tersimpan",
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// ============= TAB 1: TASK STUDIO ==========
// ==========================================
@Composable
fun TaskStudioTab(
    uiState: ArchTaskUiState,
    onProgressChange: (Task, Float) -> Unit,
    onDeleteClick: (Task) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterPhase by remember { mutableStateOf("Semua") }
    val phases = listOf("Semua", "Concept", "Modeling", "Working Docs")

    // Filter tasks safely
    val filteredTasks = uiState.tasks.filter { task ->
        val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) ||
                task.project.contains(searchQuery, ignoreCase = true) ||
                task.designer.contains(searchQuery, ignoreCase = true)
        val matchesPhase = selectedFilterPhase == "Semua" || 
                task.phase.contains(selectedFilterPhase, ignoreCase = true)
        matchesSearch && matchesPhase
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        // Search & Phase Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari tugas, proyek, desainer...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("search_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                textStyle = TextStyle(fontSize = 12.sp),
                singleLine = true
            )

            // Select Phase Filters List
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(52.dp)
            ) {
                var expanded by remember { mutableStateOf(false) }
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "", modifier = Modifier.size(16.dp))
                        Text(selectedFilterPhase, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    phases.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p) },
                            onClick = {
                                selectedFilterPhase = p
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // List Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredTasks.size} Draf Tugas Arsitektur",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (selectedFilterPhase != "Semua") {
                Text(
                    text = "Filter: $selectedFilterPhase",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { selectedFilterPhase = "Semua" }
                )
            }
        }

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Draw,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "Tidak Ada Tugas Ditemukan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Silakan tambah tugas baru studio arsitektur Anda.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("task_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskRowItem(
                        task = task,
                        onProgressChange = { progress -> onProgressChange(task, progress) },
                        onDeleteClick = { onDeleteClick(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskRowItem(
    task: Task,
    onProgressChange: (Float) -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp)
        ) {
            // Priority Tag Row & ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority Color Dot
                    val priorityColor = when (task.priority.lowercase()) {
                        "high" -> Color(0xFFFF5252)
                        "medium" -> Color(0xFFFFC107)
                        else -> Color(0xFF4CAF50)
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(priorityColor, shape = CircleShape)
                    )
                    Text(
                        text = "Prioritas: ${task.priority}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Cloud Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (task.cloudSynced) Icons.Default.CloudDone else Icons.Default.CloudSync,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (task.cloudSynced) Color(0xFF4CAF50) else Color(0xFF9EABB8)
                    )
                    Text(
                        text = if (task.cloudSynced) "Synced" else "Local Draft",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Project Name
            Text(
                text = task.project.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.sp
            )

            // Task Title
            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar & Info Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Progress Gauge Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${task.progress.toInt()}% Selesai",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = task.phase,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Date Deadline
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val isUrgent = task.deadline < System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L) && task.status != "Completed"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (isUrgent) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sdf.format(Date(task.deadline)),
                        fontSize = 11.sp,
                        fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isUrgent) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded Controls panel
            if (expanded) {
                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )

                // Designer Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Penanggung Jawab: ${task.designer}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (task.hasReminder) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Pengingat Lonceng",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Catatan Studio: ${task.notes}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // INTERACTIVE PROGRESS CONTROLLER (SLIDER)
                Text(
                    text = "Geser untuk Mengubah Progres Tugas:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Slider(
                        value = task.progress,
                        onValueChange = { onProgressChange(it) },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${task.progress.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }

                // Delete Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.testTag("delete_task_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Tugas",
                            tint = Color(0xFFFF5252).copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// ============= TAB 2: ANALYTICS & DOCK ======
// ==========================================
@Composable
fun AnalyticsDashboardTab(
    uiState: ArchTaskUiState,
    onE2eeToggle: () -> Unit,
    onChangeStorageClick: () -> Unit,
    onPdfExportClick: () -> Unit
) {
    val totalTasks = uiState.tasks.size
    val completedTasks = uiState.tasks.count { it.status == "Completed" }
    val inProgress = uiState.tasks.count { it.status == "In Progress" }
    val todoCount = uiState.tasks.count { it.status == "Todo" }

    val avgProgress = if (totalTasks > 0) {
        uiState.tasks.map { it.progress }.average().toFloat()
    } else {
        0f
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Stats Metrics Grid
        item {
            Text(
                text = "Statistik Produktivitas Studio",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Projects
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text("TOTAL TUGAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text("$totalTasks", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Draf di database Room", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Average Progress Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text("KEMAJUAN RATA2", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("${avgProgress.toInt()}%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Makin dekat target!", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Sub Stats Circles Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Menunggu", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$todoCount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sedang Kerja", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$inProgress", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Selesai", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE8F5E9), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$completedTasks", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF4CAF50))
                        }
                    }
                }
            }
        }

        // Custom Visual Vector Chart Draw
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "Tren Kegiatan Studio Mingguan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Metrik volume perpanjangan blueprint & ketepatan revisi",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw curves via custom canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Draw Grid Lines helper
                        val gridPaintVal = 40f
                        for (i in 1..3) {
                            val y = height - (i * (height / 4))
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 2f
                            )
                        }

                        // Coordinates representation
                        val points = listOf(
                            Offset(0.05f * width, 0.8f * height),   // Mon
                            Offset(0.2f * width, 0.75f * height),  // Tue
                            Offset(0.4f * width, 0.45f * height),  // Wed
                            Offset(0.6f * width, 0.35f * height),  // Thu
                            Offset(0.8f * width, 0.65f * height),  // Fri
                            Offset(0.95f * width, 0.2f * height)   // Sat-Sun (Sync Peaks)
                        )

                        // Draw gradient filling under line
                        val pathBrush = Brush.verticalGradient(
                            colors = listOf(
                                BlueprintPrimary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )

                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (j in 1 until points.size) {
                                cubicTo(
                                    (points[j - 1].x + points[j].x) / 2, points[j - 1].y,
                                    (points[j - 1].x + points[j].x) / 2, points[j].y,
                                    points[j].x, points[j].y
                                )
                            }
                        }

                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(path)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }

                        drawPath(fillPath, brush = pathBrush)

                        // Draw curve line
                        drawPath(
                            path = path,
                            color = BlueprintPrimary,
                            style = Stroke(width = 6f, cap = StrokeCap.Round)
                        )

                        // Draw touch dots
                        points.forEachIndexed { i, p ->
                            drawCircle(
                                color = if (i == points.size - 1) TerracottaSecondary else BlueprintPrimary,
                                radius = 10f,
                                center = p
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 5f,
                                center = p
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val labels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab/Min")
                        labels.forEach { label ->
                            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Action Quick Utilities (Export reports, custom sync destination, security keys)
        item {
            Text(
                text = "Tindakan Kolaborasi & Keamanan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Export PDF Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFFF5252)
                            )
                            Column {
                                Text("Ekspor Laporan PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Otomatis rangkum seluruh progres tugas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Button(
                            onClick = onPdfExportClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_pdf_button")
                        ) {
                            Text("Unduh PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Cloud selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically, // correct key
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderShared,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text("Folder Dokumen Kolaborasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Hubungkan folder utama gambar rancang", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Button(
                            onClick = onChangeStorageClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Koneksi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // E2EE Setup Lock toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (uiState.isE2eeEnabled) Icons.Default.AdminPanelSettings else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (uiState.isE2eeEnabled) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                            Column {
                                Text("Enkripsi End-To-End (E2EE)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Jamin kerahasiaan draft perusahaan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = uiState.isE2eeEnabled,
                            onCheckedChange = { onE2eeToggle() }
                        )
                    }
                }
            }
        }

        // Third Party API integration note
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ElectricBolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Column {
                        Text(
                            text = "Integrasi API Pihak Ketiga (BIM / AutoCAD)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Webhooks aktif untuk trigger otomatis sinkronisasi status gambar dari Revit / AutoCAD Autodesk ke server ArchiTask.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// ============= TAB 3: AI ANALISI PDF ======
// ==========================================
@Composable
fun AiPdfAnalyzerTab(
    uiState: ArchTaskUiState,
    onPresetSelect: (String) -> Unit,
    onCustomContentChange: (String) -> Unit,
    onAnalyzeClick: (String) -> Unit
) {
    val presets = mapOf(
        "Villa Canggu Structural Layout Specs" to """
            Draf Spesifikasi Teknis Struktur Villa Canggu.
            - Bentangan atap beton kantilever overhang sepanjang 4.2 meter tanpa tiang penyangga sudut konstruksi.
            - Penggunaan dinding kaca struktural tempered glass tebal 12mm merangkap partisi luar pembatas ruang makan utama tebing.
            - Tinggi lantai-ke-langit (floor-to-ceiling) setinggi 3.8 meter.
            - Area tanah merupakan rawa reklamasi dengan air laut asin abrasi tinggi (jarak 100m dari pantai Canggu).
            - Rangka atap sekunder menggunakan besi hollow karat galvanis.
        """.trimIndent(),

        "Ubud Eco-Resort Green Audit" to """
            Hasil Draf Laporan Audit Desain Hijau Eco-Resort Ubud, Bali.
            - Struktur pilar utama menggunakan batang bambu petung raksasa dengan anyaman tali ijuk tradisi lisan aspal ramah lingkungan.
            - Lokasi diletakkan di lereng miring ekstrim 45 derajat langsung menghadap aliran deras sungai Ayung Sayan.
            - Tidak disediakan AC pendingin udara; murni ventilasi menyilang bukaan teras jaring penangkap angin lembap hutan rimba.
            - Kamar mandi semi terbuka dilengkapi sistem pembuangan Constructed Wetlands penyaring air sabun lindi mandiri.
            - Batu paras lokal dipergunakan merata sebagai finishing penahan deformasi seismik tanah berundak.
        """.trimIndent(),

        "Urban Highrise Smart-Facade Jakarta" to """
            Draf Desain Fasad Gedung Perkantoran ArchiOffice Hub Jakarta Pusat.
            - Selubung tirai kaca ganda (double-glazed smart facade) terhubung aktuator sensor gerak louvre kayu otomatis.
            - Konstruksi menggunakan modular beton pracetak pascabayar berkekuatan sangat tinggi di pusat getaran jalan tol MT Haryono.
            - Atap dirancang menjadi taman atap fungsional (Green Roof) dengan ketebalan media tanah pupuk berbobot beban 350kg/m2.
            - Integrasi penuh solar sel panel surya monokristal terdistribusi hemat daya 250m2.
        """.trimIndent()
    )

    val currentPresetContent = presets[uiState.selectedDocPreset] ?: ""

    // Auto update content if user selects a preset
    LaunchedEffect(uiState.selectedDocPreset) {
        onCustomContentChange(currentPresetContent)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "Konversi PDF & Draf Analisis Desain AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Gunakan berkas draf laporan blueprint dari perangkat atau salin teks PDF arsitektur untuk dianotasi oleh Gemini AI secara otomatis.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Select Dropdown
                    Text(
                        text = "Pilih Preset Dokumen PDF Blueprint:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uiState.selectedDocPreset,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Start,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                presets.keys.forEach { name ->
                                    DropdownMenuItem(
                                        text = { Text(name, fontSize = 12.sp) },
                                        onClick = {
                                            onPresetSelect(name)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Custom Copy Input Field
                    Text(
                        text = "Atau Salin/Edit Dokumen Spesifikasi PDF:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.customPdfContent,
                        onValueChange = { onCustomContentChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("pdf_spec_input"),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
                        placeholder = { Text("Masukkan spesifikasi desain arsitektural...", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        maxLines = 10
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Trigger button with Loader
                    Button(
                        onClick = { onAnalyzeClick(uiState.customPdfContent) },
                        enabled = !uiState.isAnalyzing && uiState.customPdfContent.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("analyze_ai_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState.isAnalyzing) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                Text("Menganalisis draf via Gemini 3.5...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Mulai Analisis Desain AI", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Show parsing advice if available
        if (uiState.apiError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        Text(
                            text = uiState.apiError ?: "",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }

        val result = uiState.analysisResult
        if (result != null) {
            item {
                Text(
                    text = "Laporan Evaluasi Desain AI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Complexity Widget & Title
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MODUL EVALUASI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = result.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Evaluasi Material: ${result.materialEfficiency}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Circular Gauge for Complexity
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val complexColor = when {
                                result.complexityScore > 85 -> Color(0xFFFF5252)
                                result.complexityScore > 60 -> Color(0xFFFFC107)
                                else -> Color(0xFF4CAF50)
                            }
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color(0xFFEFF1F8),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = complexColor,
                                    startAngle = -90f,
                                    sweepAngle = (result.complexityScore / 100f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${result.complexityScore}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Kompleks",
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Strengths / Kelebihan
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Text("Kekuatan & Kelebihan Desain:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4CAF50))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        result.strengths.forEachIndexed { idx, s ->
                            Text(
                                text = "${idx + 1}. $s",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }

            // Risks / Risiko
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            Text("Risiko & Kerentanan Struktural:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFF5252))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        result.structuralRisks.forEachIndexed { idx, r ->
                            Text(
                                text = "${idx + 1}. $r",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }

            // AI Recommendations & Advices
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MinimalistAiBg
                    ),
                    border = BorderStroke(1.dp, MinimalistAiBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = MinimalistAiIcon, modifier = Modifier.size(16.dp))
                            Text("Rekomendasi Otomatis (Saran Gemini):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MinimalistAiText)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        result.suggestions.forEachIndexed { idx, sug ->
                            Text(
                                text = "➜ $sug",
                                fontSize = 11.sp,
                                color = MinimalistAiBodyText,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ============= DIALOG OVERLAYS ============
// ==========================================

// Add task draft form sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdded: (String, String, String, String, String, String, String, Int, Boolean, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("") }
    var designer by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf("Concept") }
    var priority by remember { mutableStateOf("High") }
    var status by remember { mutableStateOf("Todo") }
    var deadlineDays by remember { mutableStateOf("3") }
    var hasReminder by remember { mutableStateOf(true) }
    var reminderMinutesBefore by remember { mutableStateOf("60") }

    val phases = listOf("Concept", "Modeling", "Working Docs")
    val priorities = listOf("High", "Medium", "Low")
    val statuses = listOf("Todo", "In Progress", "In Review", "Completed")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tambah Draf Tugas Studio", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_task_form"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Task Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama Tugas / Blueprint", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Project
                item {
                    OutlinedTextField(
                        value = project,
                        onValueChange = { project = it },
                        label = { Text("Nama Proyek Konstruksi", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Designer
                item {
                    OutlinedTextField(
                        value = designer,
                        onValueChange = { designer = it },
                        label = { Text("Penanggung Jawab (Arsitek)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Phase selection
                item {
                    Text("Fase Desain:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        phases.forEach { p ->
                            FilterChip(
                                selected = phase == p,
                                onClick = { phase = p },
                                label = { Text(p, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // Priority & Status selects
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Prioritas:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            var expandedP by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { expandedP = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(priority, fontSize = 11.sp)
                                }
                                DropdownMenu(expanded = expandedP, onDismissRequest = { expandedP = false }) {
                                    priorities.forEach { p ->
                                        DropdownMenuItem(text = { Text(p) }, onClick = { priority = p; expandedP = false })
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Status Awal:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            var expandedS by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { expandedS = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(status, fontSize = 11.sp)
                                }
                                DropdownMenu(expanded = expandedS, onDismissRequest = { expandedS = false }) {
                                    statuses.forEach { s ->
                                        DropdownMenuItem(text = { Text(s) }, onClick = { status = s; expandedS = false })
                                    }
                                }
                            }
                        }
                    }
                }

                // Target Date in Days representation
                item {
                    OutlinedTextField(
                        value = deadlineDays,
                        onValueChange = { deadlineDays = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Tenggat Batas Waktu (Hari dari sekarang)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Notes Detail
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan Studio", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3
                    )
                }

                // Active automatic reminders setups
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sistem Alarm Pengingat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Kirim push notifikasi otomatis", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = hasReminder, onCheckedChange = { hasReminder = it })
                    }
                }

                if (hasReminder) {
                    item {
                        OutlinedTextField(
                            value = reminderMinutesBefore,
                            onValueChange = { reminderMinutesBefore = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Ingatkan Sebelum Tenggat (Menit)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = deadlineDays.toIntOrNull() ?: 3
                    val remMin = reminderMinutesBefore.toIntOrNull() ?: 60
                    onTaskAdded(title, project, designer, phase, priority, status, notes, days, hasReminder, remMin)
                },
                modifier = Modifier.testTag("form_submit_button")
            ) {
                Text("Tambah Tugas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

// Notifications overlay dialog list
@Composable
fun NotificationsDialog(
    notifications: List<NotificationItem>,
    onClose: () -> Unit,
    onClear: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Log & Alarm Real-Time", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    TextButton(onClick = onClear) {
                        Text("Bersihkan", fontSize = 11.sp, color = Color.Red)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada notifikasi baru", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(item.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(item.timestamp)),
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text("Tutup Log")
                }
            }
        }
    }
}

// Storage selector Dialog
@Composable
fun StorageDialog(
    currentStorage: String,
    onDismiss: () -> Unit,
    onStorageConnected: (String) -> Unit
) {
    val options = listOf(
        "Google Drive (Studio Shared)",
        "Dropbox ArchiCloud Platform",
        "Local NAS Server (Canggu Studio)",
        "Microsoft OneDrive Professional"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Penyimpanan Kolaborasi Tim", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Pilih repositori cloud untuk mengunggah otomatis draf PDF dan draf gambar AutoCAD AutoCAD/Revit Revit:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                options.forEach { opt ->
                    val isSel = opt == currentStorage
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStorageConnected(opt) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(opt, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            if (isSel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

// Breathtaking interactive PDF Export summary preview Dialog
@Composable
fun PdfExportDialog(
    tasks: List<Task>,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val formattedDate = sdf.format(Date())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE4E7EB)) // light slate lookalike representation of a paper
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Actions Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ekspor Pratinjau PDF", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1F2937))

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, contentDescription = "Bagi", tint = BlueprintPrimary)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Black)
                        }
                    }
                }

                // PDF paper container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, Color(0xFFD1D5DB)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        // Letterhead
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "ARCHITASK METRIC REPORT",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = BlueprintPrimary
                                    )
                                    Text(
                                        text = "Modern Architecture Studio Inc.",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("TANGGAL: $formattedDate", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Text("ID: AR-REP-2026", fontSize = 8.sp, color = Color.Gray)
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.Black, thickness = 2.dp)
                        }

                        // Summary Statistics
                        item {
                            Text(
                                text = "RINGKASAN STATUS BLUEPRINT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F4F6))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("TOTAL TUGAS", fontSize = 8.sp, color = Color.Gray)
                                    Text("${tasks.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("RATA2 PROG", fontSize = 8.sp, color = Color.Gray)
                                    val avg = if (tasks.isNotEmpty()) tasks.map { it.progress }.average().toInt() else 0
                                    Text("$avg%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BlueprintPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SELESAI", fontSize = 8.sp, color = Color.Gray)
                                    Text("${tasks.count { it.status == "Completed" }}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Tasks list formatting
                        item {
                            Text(
                                text = "TABEL RINCIAN TUGAS AKTIF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        items(tasks) { task ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(task.title, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Black)
                                        Text("${task.project} — ${task.designer} [${task.phase}]", fontSize = 8.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${task.progress.toInt()}%", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = BlueprintPrimary)
                                        Text(task.status, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                // Horizontal visual bar lines in PDF paper
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color(0xFFE5E7EB))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(task.progress / 100f)
                                            .background(BlueprintPrimary)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Divider(color = Color(0xFFF3F4F6))
                            }
                        }

                        // Authenticator signatures
                        item {
                            Spacer(modifier = Modifier.height(30.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Disiapkan Oleh,", fontSize = 8.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text("ArchiTask Pro System", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.Black)
                                    Text("Sistem Log Terenkripsi", fontSize = 8.sp, color = Color.Gray)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Disetujui Oleh,", fontSize = 8.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text("Lead Studio Architect", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.Black)
                                    Text("Studio Principal", fontSize = 8.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Export buttons Row
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueprintPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Text("Simpan & Unduh Berkas PDF Resmi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
