package com.example.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.crypto.FileCryptoManager
import com.example.data.model.VaultCategory
import com.example.data.model.VaultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class VaultRepository(private val context: Context) {

    private val vaultDir = File(context.filesDir, "vault_storage").apply {
        if (!exists()) mkdirs()
    }
    private val metaFile = File(context.filesDir, "vault_metadata.json")

    private val _items = MutableStateFlow<List<VaultItem>>(emptyList())
    val items: StateFlow<List<VaultItem>> = _items.asStateFlow()

    init {
        loadMetadata()
    }

    @Synchronized
    private fun loadMetadata() {
        if (!metaFile.exists()) {
            _items.value = emptyList()
            return
        }
        try {
            val jsonStr = metaFile.readText()
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<VaultItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val catStr = obj.optString("category", VaultCategory.OTHER.name)
                val cat = try {
                    VaultCategory.valueOf(catStr)
                } catch (e: Exception) {
                    VaultCategory.OTHER
                }
                list.add(
                    VaultItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        originalFileName = obj.getString("originalFileName"),
                        mimeType = obj.getString("mimeType"),
                        fileSize = obj.getLong("fileSize"),
                        category = cat,
                        localEncryptedPath = obj.getString("localEncryptedPath"),
                        createdAt = obj.getLong("createdAt"),
                        checksum = obj.optString("checksum", null)
                    )
                )
            }
            _items.value = list
        } catch (e: Exception) {
            e.printStackTrace()
            _items.value = emptyList()
        }
    }

    @Synchronized
    private fun saveMetadata(itemsToSave: List<VaultItem>) {
        try {
            val jsonArray = JSONArray()
            itemsToSave.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("originalFileName", item.originalFileName)
                    put("mimeType", item.mimeType)
                    put("fileSize", item.fileSize)
                    put("category", item.category.name)
                    put("localEncryptedPath", item.localEncryptedPath)
                    put("createdAt", item.createdAt)
                    item.checksum?.let { put("checksum", it) }
                }
                jsonArray.put(obj)
            }
            val tempFile = File(context.filesDir, "vault_metadata.tmp")
            tempFile.writeText(jsonArray.toString())
            tempFile.renameTo(metaFile)
            _items.value = itemsToSave
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun detectCategory(mimeType: String, fileName: String): VaultCategory {
        val lowerMime = mimeType.lowercase()
        val lowerName = fileName.lowercase()
        return when {
            lowerMime.startsWith("image/") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                    lowerName.endsWith(".png") || lowerName.endsWith(".webp") || lowerName.endsWith(".gif") -> VaultCategory.PHOTOS

            lowerMime.startsWith("video/") || lowerName.endsWith(".mp4") || lowerName.endsWith(".mkv") ||
                    lowerName.endsWith(".webm") || lowerName.endsWith(".mov") -> VaultCategory.VIDEOS

            lowerMime.startsWith("text/") || lowerMime.contains("pdf") || lowerMime.contains("document") ||
                    lowerMime.contains("msword") || lowerName.endsWith(".pdf") || lowerName.endsWith(".txt") ||
                    lowerName.endsWith(".docx") || lowerName.endsWith(".xlsx") || lowerName.endsWith(".pptx") -> VaultCategory.DOCUMENTS

            lowerMime.contains("zip") || lowerMime.contains("compressed") || lowerMime.contains("tar") ||
                    lowerName.endsWith(".zip") || lowerName.endsWith(".rar") || lowerName.endsWith(".7z") ||
                    lowerName.endsWith(".tar.gz") -> VaultCategory.ARCHIVES

            else -> VaultCategory.OTHER
        }
    }

    suspend fun importFile(uri: Uri): Result<VaultItem> = withContext(Dispatchers.IO) {
        try {
            val contentResolver: ContentResolver = context.contentResolver
            var displayName = "file_${System.currentTimeMillis()}"
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) displayName = cursor.getString(nameIndex) ?: displayName
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val fileId = UUID.randomUUID().toString()
            val encryptedFile = File(vaultDir, "$fileId.enc")

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileCryptoManager.encryptStreamToFile(inputStream, encryptedFile)
            } ?: return@withContext Result.failure(Exception("Tidak dapat membuka file yang dipilih"))

            if (fileSize == 0L) {
                fileSize = encryptedFile.length()
            }
            val checksum = FileCryptoManager.calculateMd5(encryptedFile)
            val category = detectCategory(mimeType, displayName)

            val newItem = VaultItem(
                id = fileId,
                name = displayName,
                originalFileName = displayName,
                mimeType = mimeType,
                fileSize = fileSize,
                category = category,
                localEncryptedPath = encryptedFile.absolutePath,
                createdAt = System.currentTimeMillis(),
                checksum = checksum
            )

            val currentList = _items.value.toMutableList()
            currentList.add(0, newItem)
            saveMetadata(currentList)

            Result.success(newItem)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Membuat catatan rahasia terenkripsi (teks/memo/password) langsung di brankas
     */
    suspend fun createSecretNote(title: String, content: String): Result<VaultItem> = withContext(Dispatchers.IO) {
        try {
            val cleanTitle = if (title.isBlank()) "Catatan Rahasia" else title.trim()
            val fileName = "$cleanTitle.txt"
            val fileId = UUID.randomUUID().toString()
            val encryptedFile = File(vaultDir, "$fileId.enc")

            val bytes = content.toByteArray(Charsets.UTF_8)
            ByteArrayInputStream(bytes).use { inputStream ->
                FileCryptoManager.encryptStreamToFile(inputStream, encryptedFile)
            }

            val checksum = FileCryptoManager.calculateMd5(encryptedFile)

            val newItem = VaultItem(
                id = fileId,
                name = fileName,
                originalFileName = fileName,
                mimeType = "text/plain",
                fileSize = bytes.size.toLong(),
                category = VaultCategory.NOTES,
                localEncryptedPath = encryptedFile.absolutePath,
                createdAt = System.currentTimeMillis(),
                checksum = checksum
            )

            val currentList = _items.value.toMutableList()
            currentList.add(0, newItem)
            saveMetadata(currentList)

            Result.success(newItem)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun readSecretNoteContent(item: VaultItem): String? = withContext(Dispatchers.IO) {
        try {
            val tempFile = getDecryptedTempPreview(item)
            tempFile.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteItem(item: VaultItem) = withContext(Dispatchers.IO) {
        val file = File(item.localEncryptedPath)
        if (file.exists()) file.delete()
        val currentList = _items.value.filter { it.id != item.id }
        saveMetadata(currentList)
    }

    suspend fun renameItem(item: VaultItem, newName: String) = withContext(Dispatchers.IO) {
        val updated = item.copy(name = newName.trim())
        val currentList = _items.value.map { if (it.id == item.id) updated else it }
        saveMetadata(currentList)
    }

    fun getDecryptedTempPreview(item: VaultItem): File {
        val encFile = File(item.localEncryptedPath)
        return FileCryptoManager.decryptToTempFile(context, encFile, item.name)
    }

    suspend fun exportFileToUri(item: VaultItem, destUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encFile = File(item.localEncryptedPath)
            if (!encFile.exists()) {
                return@withContext Result.failure(Exception("File terenkripsi lokal tidak ditemukan"))
            }
            context.contentResolver.openOutputStream(destUri)?.use { outputStream ->
                FileCryptoManager.decryptFileToStream(encFile, outputStream)
            } ?: return@withContext Result.failure(Exception("Gagal membuka lokasi penyimpanan ekspor"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cadangan Lokal Mandiri (Self-Contained Local Backup):
     * Mengemas seluruh berkas terenkripsi dan metadata ke file ZIP tunggal di penyimpanan perangkat.
     */
    suspend fun exportVaultBackupZip(destUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val list = _items.value
            val outputStream = context.contentResolver.openOutputStream(destUri)
                ?: return@withContext Result.failure(Exception("Gagal membuka lokasi cadangan"))

            ZipOutputStream(outputStream).use { zos ->
                // Masukkan metadata
                if (metaFile.exists()) {
                    zos.putNextEntry(ZipEntry("vault_metadata.json"))
                    metaFile.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }

                // Masukkan tiap file terenkripsi
                list.forEach { item ->
                    val file = File(item.localEncryptedPath)
                    if (file.exists()) {
                        zos.putNextEntry(ZipEntry("files/${file.name}"))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            Result.success(list.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Memulihkan Brankas dari file ZIP cadangan lokal
     */
    suspend fun restoreVaultBackupZip(srcUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(srcUri)
                ?: return@withContext Result.failure(Exception("Gagal membaca file cadangan"))

            var restoredFiles = 0
            var restoredMetaJson: String? = null

            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "vault_metadata.json") {
                        restoredMetaJson = zis.bufferedReader().readText()
                    } else if (entry.name.startsWith("files/") && !entry.isDirectory) {
                        val fileName = entry.name.removePrefix("files/")
                        val targetFile = File(vaultDir, fileName)
                        targetFile.outputStream().use { zis.copyTo(it) }
                        restoredFiles++
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            if (restoredMetaJson != null) {
                metaFile.writeText(restoredMetaJson!!)
                loadMetadata()
            }

            Result.success(restoredFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllVaultData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            vaultDir.listFiles()?.forEach { it.delete() }
            if (metaFile.exists()) metaFile.delete()
            _items.value = emptyList()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
