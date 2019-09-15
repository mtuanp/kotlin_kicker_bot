package de.kicker.bot.service

import de.kicker.bot.api.MatchInteraction.*
import de.kicker.bot.slack.model.*
import me.ramswaroop.jbot.core.slack.models.Attachment
import me.ramswaroop.jbot.core.slack.models.RichMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate


@Service
class SlackMessageService {
    internal var logger: Logger = LoggerFactory.getLogger(SlackMessageService::class.java)

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var slackApiEndpoints: SlackApiEndpoints

    @Autowired
    lateinit var slackTokenService: SlackTokenService

    private fun helloAttachment() = Attachment().apply {
        text = "<!here> Hello, it's time for a Kicker match :soccer:"
        color = "#fbf4dd"
    }

    private fun actualPlayersAttachment(actualPlayersAsString: String) = Attachment().apply {
        text = "Actual players: $actualPlayersAsString"
        color = "#7CD197"
    }

    private fun interactionButtonAttachment(uuid: String) = ActionableAttachment().apply {
        title = "Would you like to join?"
        fallback = "You are unable to join the game"
        callbackId = uuid
        color = "#3AA3E3"
        actions = arrayOf(Action().apply {
            name = "plus"
            text = ":heavy_plus_sign:"
            type = "button"
            value = JOIN_MATCH.toString()
            style = "primary"
        }, Action().apply {
            name = "minus"
            text = ":heavy_minus_sign:"
            type = "button"
            value = LEAVE_MATCH.toString()
        }, Action().apply {
            name = "cancel"
            text = ":x:"
            type = "button"
            value = CANCEL_MATCH.toString()
            style = "danger"
            confirm = ConfirmDialog().apply {
                title = "Are you sure?"
                text = "Do you really want to close the kicker match?"
                okText = "Yes"
                dismissText = "No"
            }
        })
    }

    private fun matchIsReadyAttachment() = Attachment().apply {
        text = "All Players get ready for the match!"
        color = "#FFFF66"
    }

    fun postInitialMessageToChannel(uuid: String, actualPlayersAsString: String, responseUrl: String) {
        val richMessage = RichMessage().apply {
            responseType = "in_channel"
            attachments = arrayOf(helloAttachment(), actualPlayersAttachment(actualPlayersAsString), interactionButtonAttachment(uuid))
        }
        try {
            restTemplate.postForEntity(responseUrl, richMessage, String::class.java)
        } catch (e: RestClientException) {
            logger.error("couldn't send message to slack", e)
        }
    }

    fun postInitialReadyMatchMessageToChannel(actualPlayersAsString: String, responseUrl: String) {
        val richMessage = RichMessage().apply {
            responseType = "in_channel"
            attachments = arrayOf(helloAttachment(), actualPlayersAttachment(actualPlayersAsString), matchIsReadyAttachment())
        }
        try {
            restTemplate.postForEntity(responseUrl, richMessage, String::class.java)
        } catch (e: RestClientException) {
            logger.error("couldn't send message to slack", e)
        }
    }

    fun createAddPlayerMessage(originActionAttachment: Attachment, actualPlayersAsString: String, matchIsReady: Boolean): RichMessage {
        return RichMessage().apply {
            responseType = "in_channel"
            attachments = arrayOf(helloAttachment(), actualPlayersAttachment(actualPlayersAsString),
                    when {
                        matchIsReady -> matchIsReadyAttachment()
                        else -> originActionAttachment
                    })
        }
    }

    fun createMatchTimeoutMessage(): RichMessage {
        return RichMessage().apply {
            responseType = "in_channel"
            attachments = arrayOf(
                    Attachment().apply {
                        text = ":no_entry: Kicker match is no longer available, please start a new one with /kicker. :no_entry:"
                        color = "#FF1919"
                        thumbUrl = "thumb_url"
                    }
            )
        }
    }

    fun createMatchCanceledMessage(): RichMessage {
        return RichMessage().apply {
            responseType = "in_channel"
            attachments = arrayOf(
                    Attachment().apply {
                        text = ":no_entry: Kicker match has been canceled by user. :no_entry:"
                        color = "#FF1919"
                        thumbUrl = "thumb_url"
                    }
            )
        }
    }

    fun postUserGoMessageNotification(teamId: String, playerId: String, actualPlayersAsString: String) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val token = slackTokenService.getToken(teamId)
        headers.setBearerAuth(token)
        val postMessage = RichMessage().apply {
            attachments = arrayOf(Attachment().apply {
                text = "Go go go! Meet $actualPlayersAsString at the kicker."
                color = "#7CD197"
            })
            channel = playerId
        }
        try {
            val request = HttpEntity(postMessage, headers)
            restTemplate.postForEntity(slackApiEndpoints.chat().postMessage(), request, String::class.java)
        } catch (e: RestClientException) {
            logger.error("couldn't send message to slack", e)
        }
    }

    fun postAuthTokenVerifierAndSave(code: String): Boolean {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val map = LinkedMultiValueMap<String, String>().apply {
            add("client_id", slackTokenService.slackClientId)
            add("client_secret", slackTokenService.slackClientSecret)
            add("code", code)
            add("single_channel", "true")
        }

        try {
            val entity = HttpEntity<MultiValueMap<String, String>>(map, headers)
            val response = restTemplate.postForEntity(slackApiEndpoints.auth().access(), entity, AccessTokenResponse::class.java)
            if (response.statusCode.is2xxSuccessful && response.body!!.ok) {
                slackTokenService.saveToken(response.body!!.teamId, response.body!!.accessToken)
                return true
            }
        } catch (e: RestClientException) {
            logger.error("couldn't send message to slack", e)
        }
        return false
    }


}