package com.example

import com.example.data.model.VaultCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class UserPreferencesAndVaultTest {

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun testPinHashingIntegrity() {
        val pin = "1234"
        val hash1 = hashPin(pin)
        val hash2 = hashPin(pin)
        assertEquals(hash1, hash2)
        assertEquals(64, hash1.length) // SHA-256 produces 64 hex characters
    }

    @Test
    fun testVaultCategories() {
        val categories = VaultCategory.values()
        assertTrue(categories.contains(VaultCategory.PHOTOS))
        assertTrue(categories.contains(VaultCategory.VIDEOS))
        assertTrue(categories.contains(VaultCategory.DOCUMENTS))
        assertTrue(categories.contains(VaultCategory.NOTES))
    }
}
