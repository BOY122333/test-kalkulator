package com.example.ui.vault

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@Composable
fun VaultSettingsDialog(
    userPrefs: UserPreferencesRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val secretIpCode by userPrefs.secretIpCodeFlow.collectAsState(initial = UserPreferencesRepository.DEFAULT_SECRET_IP)
    val isBiometricEnabled by userPrefs.biometricEnabledFlow.collectAsState(initial = false)
    val displayAppName by userPrefs.displayAppNameFlow.collectAsState(initial = UserPreferencesRepository.DEFAULT_DISPLAY_APP_NAME)
    val accentTheme by userPrefs.accentThemeFlow.collectAsState(initial = UserPreferencesRepository.DEFAULT_ACCENT_THEME)
    val openingMode by userPrefs.openingModeFlow.collectAsState(initial = UserPreferencesRepository.DEFAULT_OPENING_MODE)

    var secretIpInput by remember(secretIpCode) { mutableStateOf(secretIpCode) }

    // PIN Change state
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }

    // Name disguise
    var appNameInput by remember(displayAppName) { mutableStateOf(displayAppName) }

    var showResetConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pengaturan Brankas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION: KEAMANAN & PIN
                Text(
                    text = "Keamanan & Akses",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Kunci Biometrik / Sidik Jari", fontWeight = FontWeight.SemiBold)
                                Text("Buka brankas dengan sensor sidik jari perangkat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        userPrefs.setBiometricEnabled(enabled)
                                    }
                                }
                            )
                        }

                        Divider()

                        Text("Ganti PIN Akses", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        OutlinedTextField(
                            value = currentPinInput,
                            onValueChange = { if (it.length <= 8) currentPinInput = it },
                            label = { Text("PIN Saat Ini") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 8) newPinInput = it },
                            label = { Text("PIN Baru (4-8 Digit)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { if (it.length <= 8) confirmPinInput = it },
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
                                scope.launch {
                                    val isCurrentValid = userPrefs.verifyPin(currentPinInput)
                                    if (!isCurrentValid) {
                                        pinMessage = "PIN saat ini salah!"
                                        return@launch
                                    }
                                    if (newPinInput.length < 4) {
                                        pinMessage = "PIN baru minimal 4 angka!"
                                        return@launch
                                    }
                                    if (newPinInput != confirmPinInput) {
                                        pinMessage = "Konfirmasi PIN tidak cocok!"
                                        return@launch
                                    }
                                    userPrefs.setPin(newPinInput)
                                    pinMessage = "PIN berhasil diperbarui!"
                                    currentPinInput = ""
                                    newPinInput = ""
                                    confirmPinInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = currentPinInput.isNotBlank() && newPinInput.isNotBlank() && confirmPinInput.isNotBlank()
                        ) {
                            Text("Simpan PIN Baru")
                        }
                    }
                }

                // SECTION: PEMICU KALKULATOR RAHASIA
                Text(
                    text = "Pemicu Rahasia di Kalkulator",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Ketik alamat IP ini pada kolom IP kalkulator subnet, lalu tekan tombol 'Hitung Subnet'. Brankas akan otomatis terbuka seketika!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = secretIpInput,
                            onValueChange = { secretIpInput = it.trim() },
                            label = { Text("IP Pemicu Rahasia") },
                            placeholder = { Text("192.168.99.99") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    userPrefs.setSecretIpCode(secretIpInput)
                                    Toast.makeText(context, "IP Pemicu berhasil diatur ke $secretIpInput", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = secretIpInput.isNotBlank()
                        ) {
                            Text("Simpan IP Pemicu")
                        }
                    }
                }

                // SECTION: PENYAMARAN APLIKASI
                Text(
                    text = "Penyamaran & Tampilan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Nama Tampilan Aplikasi", fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = appNameInput,
                            onValueChange = { appNameInput = it },
                            label = { Text("Judul Samaran") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    userPrefs.setDisplayAppName(appNameInput)
                                    Toast.makeText(context, "Nama tampilan disimpan!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simpan Nama")
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("Mode Layar Pembuka", fontWeight = FontWeight.SemiBold)
                        val modes = listOf(
                            "NONE" to "Langsung Kalkulator",
                            "FAKE_CRASH" to "Pesan Eror Palsu (Crash)",
                            "BANNER" to "Banner Jaringan"
                        )
                        modes.forEach { (mCode, mLabel) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch { userPrefs.setOpeningMode(mCode) }
                                    }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mLabel, fontSize = 14.sp)
                                if (openingMode == mCode) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // SECTION: RESET & BERSIHKAN
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bersihkan Log & Reset Pengaturan")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Pengaturan?") },
            text = { Text("Tindakan ini akan mengembalikan PIN ke default (1234), mereset IP pemicu, dan menghapus log audit.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            userPrefs.resetAllPreferences()
                            showResetConfirm = false
                            Toast.makeText(context, "Pengaturan direset ke default (PIN: 1234)", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Ya, Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
