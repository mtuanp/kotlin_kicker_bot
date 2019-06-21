package de.kicker.bot.web.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SlackCommandRequestVerifier {

    @Value("\${slack.signature.key}")
    private lateinit var slackSignatureKey: String

    fun verifySlackSignature(requestBody: String, timestamp: String, versionNumber : String = "v0") : Boolean {
        return false
    }

}