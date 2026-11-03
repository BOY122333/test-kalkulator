package com.example.ui.vault

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ActivityLogItem
import com.example.data.model.AppVaultItem
import com.example.data.model.VaultCategory
import com.example.data.model.VaultItem
import com.example.data.repository.AppVaultManager
import com.example.data.repository.UserPreferencesRepository
import com.example.data.repository.VaultRepository
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultMainScreen(
    vaultRepo: VaultRepository,
    userPrefs: UserPreferencesRepository,
    onLockVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appVaultManager = remember { AppVaultManager(context) }

    val vaultItems by vaultRepo.items.collectAsState()
    val secretIpCode by userPrefs.secretIpCodeFlow.collectAsState(initial = UserPreferencesRepository.DEFAULT_SECRET_IP)
    val isBiometricEnabled by userPrefs.biometricEnabledFlow.collectAsState(initial = false)
    val displayAppName by userPrefs.displayAppNameFlow.collectAsState(initial = UserPreferencesRepository.DEFAULT_DISPLAY_APP_NAME)
    val appDescription by userPrefs.appDescriptionFlow.collectAsState(initial = UserPreferencesRepository.DEFAULT_APP_DESCRIPTION)
    val accentTheme by userPrefs.accentThemeFlow.collectAsState(initial = UserPreferencesRepository.DEFAULT_ACCENT_THEME)
    val openingMode by userPrefs.openingModeFlow.collectAsState(initial = UserPreferencesRepository.DEFAULT_OPENING_MODE)
    val lockedApps by userPrefs.lockedAppsFlow.collectAsState(initial = emptySet())
    val silencedApps by userPrefs.silencedAppsFlow.collectAsState(initial = emptySet())
    val activityLogs by userPrefs.activityLogsFlow.collectAsState(initial = emptyList())

    // 6 On-Device Native Tabs:
    // 0: Brankas File, 1: Kunci Aplikasi, 2: Cadangan Lokal, 3: Penyamaran, 4: Keamanan, 5: Log Audit
    var selectedSection by remember { mutableIntStateOf(0) }
    val sectionTitles = listOf(
        "Brankas File",
        "Kunci Aplikasi",
        "Cadangan Lokal",
        "Penyamaran",
        "Keamanan",
        "Log Audit"
    )

    // Private Vault state
    var selectedCategory by remember { mutableStateOf(VaultCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val selectedItemIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedItemIds.isNotEmpty()

    // Dialogs state
    var itemToPreview by remember { mutableStateOf<VaultItem?>(null) }
    var itemToRename by remember { mutableStateOf<VaultItem?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showNewNoteDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // SAF Import Files Launcher
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                var successCount = 0
                uris.forEach { uri ->
                    val result = vaultRepo.importFile(uri)
                    if (result.isSuccess) successCount++
                }
                userPrefs.recordActivityLog("FILE_IMPORT", "Mengimpor $successCount berkas ke brankas lokal", "FILE")
                Toast.makeText(context, "$successCount berkas berhasil dienkripsi ke brankas!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Export single file launcher
    var pendingExportItem by remember { mutableStateOf<VaultItem?>(null) }
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val item = pendingExportItem
        if (uri != null && item != null) {
            scope.launch {
                val res = vaultRepo.exportFileToUri(item, uri)
                if (res.isSuccess) {
                    userPrefs.recordActivityLog("FILE_RESTORE", "Memulihkan '${item.name}' ke perangkat", "FILE")
                    Toast.makeText(context, "Berkas '${item.name}' berhasil dipulihkan ke perangkat!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Gagal memulihkan berkas: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        pendingExportItem = null
    }

    // Full Backup Export Launcher (ZIP)
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = vaultRepo.exportVaultBackupZip(uri)
                if (res.isSuccess) {
                    val count = res.getOrDefault(0)
                    userPrefs.recordActivityLog("BACKUP_EXPORT", "Mengekspor cadangan penuh ($count berkas) ke memori HP", "BACKUP")
                    Toast.makeText(context, "Cadangan berhasil dibuat ($count berkas terenkripsi)!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Gagal mencadangkan: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Full Backup Restore Launcher (ZIP)
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = vaultRepo.restoreVaultBackupZip(uri)
                if (res.isSuccess) {
                    val count = res.getOrDefault(0)
                    userPrefs.recordActivityLog("BACKUP_RESTORE", "Memulihkan brankas dari file cadangan ($count berkas)", "BACKUP")
                    Toast.makeText(context, "Brankas berhasil dipulihkan ($count berkas dimasukkan)!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Gagal memulihkan cadangan: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val filteredItems = remember(vaultItems, selectedCategory, searchQuery) {
        vaultItems.filter { item ->
            val matchCat = when (selectedCategory) {
                VaultCategory.ALL -> true
                else -> item.category == selectedCategory
            }
            val matchSearch = if (searchQuery.isBlank()) true else {
                item.name.contains(searchQuery, ignoreCase = true)
            }
            matchCat && matchSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isSelectionMode) "${selectedItemIds.size} Dipilih" else "Ruang Rahasia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enkripsi AES-256 On-Device • 100% Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedItemIds.clear() }) {
                            Icon(Icons.Default.Close, contentDescription = "Batalkan Pilihan")
                        }
                    } else {
                        IconButton(
                            onClick = onLockVault,
                            modifier = Modifier.testTag("vault_back_to_calc_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali ke Kalkulator")
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.testTag("delete_selected_vault_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Berkas Dipilih", tint = DangerRed)
                        }
                    } else {
                        if (selectedSection == 0) {
                            IconButton(onClick = { isSearchActive = !isSearchActive }) {
                                Icon(Icons.Default.Search, contentDescription = "Cari Berkas")
                            }
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("vault_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Pengaturan")
                        }

                        IconButton(
                            onClick = onLockVault,
                            modifier = Modifier.testTag("vault_lock_now_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Kunci Brankas", tint = DangerRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedSection == 0 && !isSelectionMode) {
                var showFabMenu by remember { mutableStateOf(false) }

                Box {
                    FloatingActionButton(
                        onClick = { showFabMenu = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("vault_add_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Berkas")
                    }

                    DropdownMenu(
                        expanded = showFabMenu,
                        onDismissRequest = { showFabMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Impor Berkas / Media") },
                            leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) },
                            onClick = {
                                showFabMenu = false
                                importFileLauncher.launch(arrayOf("*/*"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tulis Catatan Rahasia") },
                            leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                            onClick = {
                                showFabMenu = false
                                showNewNoteDialog = true
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Navigation Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                sectionTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab Contents
            when (selectedSection) {
                0 -> {
                    // TAB 0: BRANKAS FILE
                    PrivateVaultSection(
                        items = filteredItems,
                        totalItemsCount = vaultItems.size,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        isSearchActive = isSearchActive,
                        selectedItemIds = selectedItemIds,
                        onToggleSelect = { id ->
                            if (selectedItemIds.contains(id)) selectedItemIds.remove(id)
                            else selectedItemIds.add(id)
                        },
                        onItemClick = { item ->
                            if (isSelectionMode) {
                                if (selectedItemIds.contains(item.id)) selectedItemIds.remove(item.id)
                                else selectedItemIds.add(item.id)
                            } else {
                                itemToPreview = item
                            }
                        },
                        onItemLongClick = { item ->
                            if (!selectedItemIds.contains(item.id)) selectedItemIds.add(item.id)
                        },
                        onImportClick = {
                            importFileLauncher.launch(arrayOf("*/*"))
                        },
                        onNewNoteClick = {
                            showNewNoteDialog = true
                        }
                    )
                }
                1 -> {
                    // TAB 1: KUNCI APLIKASI
                    AppVaultSection(
                        appVaultManager = appVaultManager,
                        lockedApps = lockedApps,
                        silencedApps = silencedApps,
                        onToggleLock = { pkg, lock ->
                            scope.launch { userPrefs.toggleAppLock(pkg, lock) }
                        },
                        onToggleSilence = { pkg, silence ->
                            scope.launch { userPrefs.toggleAppSilenced(pkg, silence) }
                        }
                    )
                }
                2 -> {
                    // TAB 2: CADANGAN & PEMULIHAN LOKAL
                    LocalBackupSection(
                        vaultCount = vaultItems.size,
                        totalSizeBytes = vaultItems.sumOf { it.fileSize },
                        onExportBackup = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            exportBackupLauncher.launch("NetSubnet_Backup_$timeStamp.zip")
                        },
                        onRestoreBackup = {
                            restoreBackupLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                        }
                    )
                }
                3 -> {
                    // TAB 3: PENYAMARAN & PERSONALISASI
                    PersonalizationSection(
                        displayAppName = displayAppName,
                        appDescription = appDescription,
                        accentTheme = accentTheme,
                        openingMode = openingMode,
                        onSaveAppName = { name ->
                            scope.launch {
                                userPrefs.setDisplayAppName(name)
                                Toast.makeText(context, "Nama samaran disimpan!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSaveDescription = { desc ->
                            scope.launch {
                                userPrefs.setAppDescription(desc)
                                Toast.makeText(context, "Deskripsi samaran disimpan!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSelectTheme = { theme ->
                            scope.launch { userPrefs.setAccentTheme(theme) }
                        },
                        onSelectOpeningMode = { mode ->
                            scope.launch { userPrefs.setOpeningMode(mode) }
                        }
                    )
                }
                4 -> {
                    // TAB 4: KEAMANAN & PEMICU
                    SecuritySection(
                        secretIpCode = secretIpCode,
                        isBiometricEnabled = isBiometricEnabled,
                        onSaveSecretIp = { ip ->
                            scope.launch {
                                userPrefs.setSecretIpCode(ip)
                                Toast.makeText(context, "IP Pemicu Rahasia diatur ke: $ip", Toast.LENGTH_LONG).show()
                            }
                        },
                        onToggleBiometric = { enabled ->
                            scope.launch { userPrefs.setBiometricEnabled(enabled) }
                        },
                        onUpdatePin = { oldPin, newPin, onResult ->
                            scope.launch {
                                val valid = userPrefs.verifyPin(oldPin)
                                if (!valid) {
                                    onResult(false, "PIN saat ini salah!")
                                } else {
                                    userPrefs.setPin(newPin)
                                    onResult(true, "PIN berhasil diperbarui!")
                                }
                            }
                        }
                    )
                }
                5 -> {
                    // TAB 5: LOG AUDIT AKTIVITAS
                    ActivityLogsSection(
                        logs = activityLogs,
                        onClearLogs = {
                            scope.launch {
                                userPrefs.clearActivityLogs()
                                Toast.makeText(context, "Log aktivitas dibersihkan!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    // DIALOG: PREVIEW FILE
    itemToPreview?.let { item ->
        VaultFileViewerDialog(
            item = item,
            vaultRepo = vaultRepo,
            onDismiss = { itemToPreview = null },
            onExport = {
                pendingExportItem = item
                exportFileLauncher.launch(item.name)
            }
        )
    }

    // DIALOG: TULIS CATATAN RAHASIA
    if (showNewNoteDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewNoteDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tulis Catatan Rahasia")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Judul Catatan (misal: Password, PIN)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Isi Catatan Rahasia") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = vaultRepo.createSecretNote(noteTitle, noteContent)
                            if (res.isSuccess) {
                                userPrefs.recordActivityLog("NOTE_CREATE", "Membuat catatan rahasia: ${noteTitle.ifBlank { "Catatan" }}", "NOTE")
                                Toast.makeText(context, "Catatan rahasia disimpan & dienkripsi!", Toast.LENGTH_SHORT).show()
                                showNewNoteDialog = false
                            }
                        }
                    },
                    enabled = noteContent.isNotBlank()
                ) {
                    Text("Simpan & Enkripsi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewNoteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // DIALOG: KONFIRMASI HAPUS BANYAK
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hapus Berkas Terpilih?") },
            text = { Text("Apakah Anda yakin ingin menghapus ${selectedItemIds.size} berkas dari brankas secara permanen?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val toDelete = vaultItems.filter { selectedItemIds.contains(it.id) }
                            toDelete.forEach { vaultRepo.deleteItem(it) }
                            userPrefs.recordActivityLog("FILE_DELETE", "Menghapus ${toDelete.size} berkas dari brankas", "FILE")
                            selectedItemIds.clear()
                            showDeleteConfirmDialog = false
                            Toast.makeText(context, "${toDelete.size} berkas berhasil dihapus!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus Permanen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // DIALOG: PENGATURAN CEPAT
    if (showSettingsDialog) {
        VaultSettingsDialog(
            userPrefs = userPrefs,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

// -------------------------------------------------------------
// SECTIONS IMPLEMENTATION (100% NATIVE & ON-DEVICE)
// -------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PrivateVaultSection(
    items: List<VaultItem>,
    totalItemsCount: Int,
    selectedCategory: VaultCategory,
    onCategorySelect: (VaultCategory) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    selectedItemIds: List<String>,
    onToggleSelect: (String) -> Unit,
    onItemClick: (VaultItem) -> Unit,
    onItemLongClick: (VaultItem) -> Unit,
    onImportClick: () -> Unit,
    onNewNoteClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        AnimatedVisibility(visible = isSearchActive) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Cari nama berkas terenkripsi...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus")
                        }
                    }
                },
                singleLine = true
            )
        }

        // Category filter chips
        ScrollableTabRow(
            selectedTabIndex = VaultCategory.values().indexOf(selectedCategory),
            edgePadding = 12.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            VaultCategory.values().forEach { cat ->
                val isSel = cat == selectedCategory
                FilterChip(
                    selected = isSel,
                    onClick = { onCategorySelect(cat) },
                    label = { Text(cat.displayName) },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = if (totalItemsCount == 0) "Brankas Anda Masih Kosong" else "Tidak ada berkas yang cocok",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Semua file dienkripsi dengan standar militer AES-256 langsung pada memori perangkat tanpa perantara.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onImportClick) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Impor Berkas")
                        }
                        OutlinedButton(onClick = onNewNoteClick) {
                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Catatan Rahasia")
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.id }) { item ->
                    val isSelected = selectedItemIds.contains(item.id)
                    VaultItemCard(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onItemClick(item) },
                        onLongClick = { onItemLongClick(item) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VaultItemCard(
    item: VaultItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (item.category) {
                    VaultCategory.PHOTOS -> Icons.Default.Image
                    VaultCategory.VIDEOS -> Icons.Default.Movie
                    VaultCategory.DOCUMENTS -> Icons.Default.Description
                    VaultCategory.NOTES -> Icons.Default.EditNote
                    VaultCategory.ARCHIVES -> Icons.Default.Archive
                    else -> Icons.Default.Security
                }

                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.fileSize / 1024} KB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFormat.format(Date(item.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppVaultSection(
    appVaultManager: AppVaultManager,
    lockedApps: Set<String>,
    silencedApps: Set<String>,
    onToggleLock: (String, Boolean) -> Unit,
    onToggleSilence: (String, Boolean) -> Unit
) {
    var apps by remember { mutableStateOf<List<AppVaultItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(lockedApps, silencedApps) {
        apps = appVaultManager.getInstalledLauncherApps(lockedApps, silencedApps)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Kunci Aplikasi Terpasang", fontWeight = FontWeight.Bold)
                    Text("Kunci aplikasi di perangkat Anda agar tidak dapat dibuka orang lain tanpa PIN", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Memuat daftar aplikasi...")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apps, key = { it.packageName }) { appItem ->
                    val isLocked = lockedApps.contains(appItem.packageName)
                    val isSilenced = silencedApps.contains(appItem.packageName)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = appItem.icon,
                                contentDescription = appItem.label,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(appItem.label, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(appItem.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }

                            IconButton(onClick = { onToggleSilence(appItem.packageName, !isSilenced) }) {
                                Icon(
                                    if (isSilenced) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                                    contentDescription = "Bungkam",
                                    tint = if (isSilenced) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { onToggleLock(appItem.packageName, !isLocked) }) {
                                Icon(
                                    if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Kunci",
                                    tint = if (isLocked) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalBackupSection(
    vaultCount: Int,
    totalSizeBytes: Long,
    onExportBackup: () -> Unit,
    onRestoreBackup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Cadangan Mandiri On-Device", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    text = "Buat cadangan utuh seluruh isi brankas terenkripsi Anda ke dalam file ZIP tunggal pada memori perangkat (Folder Download, Dokumen, atau kartu SD). Tanpa server atau website eksternal!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Status Brankas Saat Ini", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Jumlah Berkas Tersimpan:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$vaultCount Berkas", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Ukuran Berkas:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${totalSizeBytes / 1024} KB", fontWeight = FontWeight.Bold)
                }

                Divider()

                Button(
                    onClick = onExportBackup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ekspor Cadangan ke File ZIP")
                }

                OutlinedButton(
                    onClick = onRestoreBackup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pulihkan dari File ZIP Cadangan")
                }
            }
        }
    }
}

@Composable
private fun PersonalizationSection(
    displayAppName: String,
    appDescription: String,
    accentTheme: String,
    openingMode: String,
    onSaveAppName: (String) -> Unit,
    onSaveDescription: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onSelectOpeningMode: (String) -> Unit
) {
    var nameState by remember(displayAppName) { mutableStateOf(displayAppName) }
    var descState by remember(appDescription) { mutableStateOf(appDescription) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nama & Deskripsi Samaran", fontWeight = FontWeight.Bold)
                Text(
                    "Nama ini akan muncul pada antarmuka kalkulator utama untuk mengecoh orang lain yang melihat layar HP Anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = nameState,
                    onValueChange = { nameState = it },
                    label = { Text("Nama Samaran Aplikasi") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descState,
                    onValueChange = { descState = it },
                    label = { Text("Deskripsi Samaran") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onSaveAppName(nameState)
                        onSaveDescription(descState)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan Identitas Samaran")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Mode Layar Pembuka (Disguise Mode)", fontWeight = FontWeight.Bold)
                val modes = listOf(
                    "NONE" to "Langsung ke Kalkulator (Tanpa Splash)",
                    "FAKE_CRASH" to "Pesan Crash Palsu (\"Aplikasi Telah Berhenti\")",
                    "BANNER" to "Banner Jaringan Diagnostik"
                )

                modes.forEach { (code, title) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectOpeningMode(code) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            if (code == "FAKE_CRASH") {
                                Text(
                                    "Menampilkan pesan eror sistem palsu ketika dibuka. Hanya Anda yang tahu menekan 'Laporkan' untuk masuk kalkulator!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (openingMode == code) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tema Warna Aksen", fontWeight = FontWeight.Bold)
                val themes = listOf(
                    "CYAN" to CyanPrimary,
                    "GOLD" to AccentGold,
                    "EMERALD" to SuccessGreen,
                    "RUBY" to DangerRed
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    themes.forEach { (tCode, tColor) ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(tColor)
                                .clickable { onSelectTheme(tCode) }
                                .border(
                                    width = if (accentTheme == tCode) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (accentTheme == tCode) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecuritySection(
    secretIpCode: String,
    isBiometricEnabled: Boolean,
    onSaveSecretIp: (String) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onUpdatePin: (String, String, (Boolean, String) -> Unit) -> Unit
) {
    var ipInput by remember(secretIpCode) { mutableStateOf(secretIpCode) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("IP Pemicu Rahasia di Kalkulator", fontWeight = FontWeight.Bold)
                Text(
                    text = "Ketik alamat IP ini pada kalkulator subnet dan tekan tombol 'Hitung Subnet'. Layar kunci brankas akan langsung terbuka secara instan!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it.trim() },
                    label = { Text("Kode IP Pemicu") },
                    placeholder = { Text("192.168.99.99") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { onSaveSecretIp(ipInput) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = ipInput.isNotBlank()
                ) {
                    Text("Simpan IP Pemicu")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kunci Biometrik / Sidik Jari", fontWeight = FontWeight.Bold)
                        Text("Buka brankas dengan sensor sidik jari perangkat Anda", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = onToggleBiometric
                    )
                }

                Divider()

                Text("Ganti PIN Brankas", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { if (it.length <= 8) oldPin = it },
                    label = { Text("PIN Lama") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 8) newPin = it },
                    label = { Text("PIN Baru (4-8 Digit)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 8) confirmPin = it },
                    label = { Text("Konfirmasi PIN Baru") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                if (pinMessage != null) {
                    Text(
                        text = pinMessage!!,
                        color = if (pinMessage!!.contains("berhasil", ignoreCase = true)) SuccessGreen else DangerRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        if (newPin.length < 4) {
                            pinMessage = "PIN minimal 4 angka!"
                            return@Button
                        }
                        if (newPin != confirmPin) {
                            pinMessage = "Konfirmasi PIN tidak cocok!"
                            return@Button
                        }
                        onUpdatePin(oldPin, newPin) { success, msg ->
                            pinMessage = msg
                            if (success) {
                                oldPin = ""
                                newPin = ""
                                confirmPin = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = oldPin.isNotBlank() && newPin.isNotBlank() && confirmPin.isNotBlank()
                ) {
                    Text("Perbarui PIN")
                }
            }
        }
    }
}

@Composable
private fun ActivityLogsSection(
    logs: List<ActivityLogItem>,
    onClearLogs: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Log Audit Keamanan (${logs.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (logs.isNotEmpty()) {
                TextButton(onClick = onClearLogs) {
                    Text("Bersihkan Log", color = DangerRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada catatan aktivitas", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { logItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (logItem.type) {
                                            "SECURITY" -> DangerRed.copy(alpha = 0.2f)
                                            "FILE" -> CyanPrimary.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when (logItem.type) {
                                        "SECURITY" -> Icons.Default.Security
                                        "FILE" -> Icons.Default.Description
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    tint = when (logItem.type) {
                                        "SECURITY" -> DangerRed
                                        "FILE" -> CyanPrimary
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(logItem.details, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${logItem.action} • ${dateFormat.format(Date(logItem.timestamp))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
