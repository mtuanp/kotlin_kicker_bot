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


/**
 * Service for creating slack messages and post it to slack.
 */
@Service
class SlackMessageService {
    internal var logger: Logger = LoggerFactory.getLogger(SlackMessageService::class.java)

    /**
     * Rest client for accessing slack api.
     */
    @Autowired
    lateinit var restTemplate: RestTemplate

    /**
     *  Slack endpoints information.
     */
    @Autowired
    lateinit var slackApiEndpoints: SlackApiEndpoints

    /**
     * Slack token service for retrieving the token and save it.
     */
    @Autowired
    lateinit var slackTokenService: SlackTokenService

    /**
     * Create a the initial welcome attachment.
     */
    private fun helloAttachment() = Attachment().apply {
        text = "<!here> Hello, it's time for a Kicker match :soccer:"
        color = "#fbf4dd"
    }

    /**
     * create the actual player attachment.
     */
    private fun actualPlayersAttachment(actualPlayersAsString: String) = Attachment().apply {
        text = "Actual players: $actualPlayersAsString"
        color = "#7CD197"
    }

    /**
     * create the interactive button attachment for join, leave or cancel the match.
     */
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

    /**
     * create the match ready attachment.
     */
    private fun matchIsReadyAttachment() = Attachment().apply {
        text = "All Players get ready for the match!"
        color = "#FFFF66"
    }

    /**
     * Create the initial match message and post it in the channel.
     */
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

    /**
     * Create the initial match message and post it in the channel. This is a special case, the match is already ready to play.
     */
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

    /**
     * create a active match message.
     */
    fun createActiveMatchMessage(originActionAttachment: Attachment, actualPlayersAsString: String, matchIsReady: Boolean): RichMessage {
        return RichMessage().apply {
            responseType = "in_channel"
            attachments = arrayOf(helloAttachment(), actualPlayersAttachment(actualPlayersAsString),
                    when {
                        matchIsReady -> matchIsReadyAttachment()
                        else -> originActionAttachment
                    })
        }
    }

    /**
     * create a match timeout message.
     */
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

    /**
     * create a cancel message
     */
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

    /**
     * Notifier the user for ready match.
     */
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

    /**
     * try to receive the access token from slack with the given code and save it in the token store.
     */
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