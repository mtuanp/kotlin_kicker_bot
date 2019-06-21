package de.kicker.bot.web.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec



/**
 * CLass for verified the slack request. It create a signature from the request body, timestamp and version number and check against the given signature.
 */
@Component
class SlackCommandRequestVerifier(@Value("\${slack.signature.key}") private val slackSignatureKey: String) {

    private val hashAlgorithm = "HmacSHA256"

    /**
     * Create a signature from the request body, timestamp and version number and check against the given signature. True if the given signature is equals the generated signature.
     */
    fun verifySlackSignature(givenSignature: String, requestBody: String, timestamp: String, versionNumber: String = "v0"): Boolean {
        val signatureContent = "$versionNumber:$timestamp:$requestBody"
        val signatureWithoutVersion = givenSignature.removePrefix("$versionNumber=")
        val generatedSignature = generateSignature(signatureContent)
        return signatureWithoutVersion == generatedSignature
    }
    
    fun generateSignature(signatureContent: String) : String {
        val hasher = Mac.getInstance(hashAlgorithm)
        hasher.init(SecretKeySpec(slackSignatureKey.toByteArray(), hashAlgorithm))
        val macResult = hasher.doFinal(signatureContent.toByteArray())
        return macResult.fold(StringBuilder(), { builder, it -> builder.append("%02x".format(it)) }).toString().toLowerCase()
    }

}