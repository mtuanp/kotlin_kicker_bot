package de.kicker.bot.service

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
    val logger: Logger = LoggerFactory.getLogger(SlackMessageService::class.java)

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var slackApiEndpoints: SlackApiEndpoints

    @Autowired
    lateinit var slackTokenService: SlackTokenService

    fun postInitialMessageToChannel(uuid: String, players: Collection<String>, responseUrl: String) {
        val richMessage = RichMessage("Hello, it's time for a Kicker match ⚽").apply {
            responseType = "in_channel"
            attachments = arrayOf(ActionableAttachment().apply {
                text = "Actual players: ${players.joinToString { "<@${it}>" }}"
                color = "#7CD197"
            }, ActionableAttachment().apply {
                title = "Would you like to join?"
                fallback = "You are unable to join the game"
                callbackId = "join_game"
                color = "#3AA3E3"
                actions = arrayOf(Action().apply {
                    name = "plus"
                    text = "+"
                    type = "button"
                    value = uuid
                })
            })
        }
        // Always remember to send the encoded message to Slack
        val encodedMessage = richMessage.encodedMessage()
        try {
            restTemplate.postForEntity(responseUrl, encodedMessage, String::class.java)
        } catch (e: RestClientException) {
            logger.error("couldn't send message to slack", e)
        }
    }

    fun prepareAddPlayerMessage(interactiveMessage: InteractiveMessage, actualPlayersAsString: String, matchIsReady: Boolean): RichMessage? {
        val richMessage = RichMessage(interactiveMessage.original_message.get().text).apply {
            responseType = "in_channel"
            attachments = arrayOf(
                    ActionableAttachment().apply {
                        text = "Actual players: ${actualPlayersAsString}"
                        color = "#7CD197"
                    },
                    when {
                        matchIsReady -> {
                            ActionableAttachment().apply {
                                text = "All Players get ready for the match!"
                                color = "#FFFF66"
                            }
                        }
                        else -> {
                            interactiveMessage.original_message.get().attachments[1]
                        }
                    }

            )
        }

        // Always remember to send the encoded message to Slack
        val encodedMessage = richMessage.encodedMessage()
        return encodedMessage
    }

    fun postUserGoMessageNotification(teamId: String, playerId: String, actualPlayersAsString: String) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val token = slackTokenService.getToken(teamId)
        headers.setBearerAuth(token)
        val postMessage = RichMessage().apply {
            attachments = arrayOf(Attachment().apply {
                text = "Go go go! Meet ${actualPlayersAsString} by the kicker."
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