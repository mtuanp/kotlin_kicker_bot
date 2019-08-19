package de.kicker.bot.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SlackTokenService {

    @Value("\${slackClientId}")
    lateinit var slackClientId: String

    @Value("\${slackClientSecret}")
    lateinit var slackClientSecret: String

    @Autowired
    lateinit var slackTokenFileStorage: SlackTokenFileStorage

    @Autowired
    lateinit var encryptionService: EncryptionService

    fun saveToken(teamId: String, token: String) {
        val encryptedToken = encryptionService.encrypt(token, slackClientSecret)
        slackTokenFileStorage.saveTokenToFile(teamId, encryptedToken)
    }

    fun getToken(teamId: String): String {
        val encryptedToken = slackTokenFileStorage.retrieveToken(teamId).orEmpty()
        return encryptionService.decrypt(encryptedToken, slackClientSecret)
    }


}