package com.example.data.model

enum class VaultCategory(val displayName: String) {
    ALL("Semua"),
    PHOTOS("Foto"),
    VIDEOS("Video"),
    DOCUMENTS("Dokumen"),
    NOTES("Catatan"),
    ARCHIVES("Arsip ZIP"),
    OTHER("Lainnya")
}

data class VaultItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val originalFileName: String,
    val mimeType: String,
    val fileSize: Long,
    val category: VaultCategory,
    val localEncryptedPath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val checksum: String? = null
)
