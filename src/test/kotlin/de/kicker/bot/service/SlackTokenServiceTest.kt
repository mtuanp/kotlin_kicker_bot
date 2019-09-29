package de.kicker.bot.service

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SlackTokenServiceTest {

    private lateinit var tokenService: SlackTokenService
    private lateinit var mockSlackTokenFileStorage: SlackTokenFileStorage
    private lateinit var mockEncryptionService: EncryptionService

    @BeforeEach
    fun setup() {
        mockSlackTokenFileStorage = mockk(relaxed = true)
        mockEncryptionService = mockk(relaxed = true)
        tokenService = SlackTokenService().apply {
            slackClientSecret = "Secret"
            slackTokenFileStorage = mockSlackTokenFileStorage
            encryptionService = mockEncryptionService
        }
    }

    @Test
    fun saveToken() {
        tokenService.saveToken("TeamA", "Token")
        verify (exactly = 1) { mockEncryptionService.encrypt("Token", "Secret") }
        verify (exactly = 1) { mockSlackTokenFileStorage.saveTokenToFile("TeamA", any()) }

    }

    @Test
    fun getToken() {
        tokenService.getToken("TeamA")
        verify (exactly = 1) { mockEncryptionService.decrypt(any(), "Secret") }
        verify (exactly = 1) { mockSlackTokenFileStorage.retrieveToken("TeamA") }
    }
}