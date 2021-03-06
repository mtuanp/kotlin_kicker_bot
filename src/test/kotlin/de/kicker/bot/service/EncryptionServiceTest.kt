package de.kicker.bot.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class EncryptionServiceTest {

    @Test
    fun encryptAndDecryptTest() {
        val testString = "This is a test"
        val password = "BS5RuNDZqaq3JUTMaH8gLMmculS0NHLK"
        val encyptService = EncryptionService()
        val encryptedText = encyptService.encrypt(testString, password)
        val encryptedText2 = encyptService.encrypt(testString, password)
        assertNotNull(encryptedText)
        assertNotEquals(encryptedText, encryptedText2)
        val decryptedText = encyptService.decrypt(encryptedText, password)
        assertEquals(testString, decryptedText)
    }

    @Test
    fun decryptTestFailed() {
        val testString = "daswtw9jrdajoaakma"
        val password = "BS5RuNDZqaq3JUTMaH8gLMmculS0NHLK"
        val encyptService = EncryptionService()
        assertThrows(IllegalArgumentException::class.java) { encyptService.decrypt(testString, password) }
    }

}