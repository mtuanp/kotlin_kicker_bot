package de.kicker.bot.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import de.kicker.bot.service.KickerMatchService
import de.kicker.bot.slack.model.Action
import de.kicker.bot.slack.model.ActionableAttachment
import de.kicker.bot.slack.model.InteractiveMessage
import de.kicker.bot.slack.model.SlackCommandRequest
import me.ramswaroop.jbot.core.slack.models.RichMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import kotlin.concurrent.thread

@RestController
class KickerBotSlackController {
    val logger: Logger = LoggerFactory.getLogger(KickerBotSlackController::class.java)

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var kickerMatchService: KickerMatchService

    @Autowired
    lateinit var slackApiEndpoints: SlackApiEndpoints

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @PostMapping("/slack/kickergame", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun startKickerGame(@ModelAttribute slackCommandRequest: SlackCommandRequest): String {
        thread(start = true) {
            val uuid = kickerMatchService.createKickerGame(slackCommandRequest.team_id, slackCommandRequest.user_id)
            val richMessage = RichMessage("Hello, it's time for a Kicker match ⚽").apply {
                responseType = "in_channel"
                attachments = arrayOf(ActionableAttachment().apply {
                    text = "Actual players: ${kickerMatchService.listMatchPlayers(uuid).joinToString { "<@${it}>" }}"
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
                restTemplate.postForEntity(slackCommandRequest.response_url, encodedMessage, String::class.java)
            } catch (e: RestClientException) {
                logger.error("couldn't send message to slack", e)
            }
        }
        return ""
    }

    @PostMapping("/slack/receive", produces = ["application/json"])
    fun callbackKickerGame(@RequestParam("payload") payload: String): RichMessage? {
        val interactiveMessage = objectMapper.readValue(payload, InteractiveMessage::class.java)
        val uuid = interactiveMessage.actions.first().value
        val addSuccess = kickerMatchService.addPlayerToMatch(uuid, interactiveMessage.team.get().id, interactiveMessage.user.get().id)
        if (addSuccess) {
            val matchIsReady = kickerMatchService.matchIsReady(uuid)
            val actualPlayers = kickerMatchService.listMatchPlayers(uuid).joinToString { "<@${it}>" }
            val richMessage = RichMessage(interactiveMessage.original_message.get().text).apply {
                responseType = "in_channel"
                attachments = arrayOf(
                        ActionableAttachment().apply {
                            text = "Actual players: ${actualPlayers}"
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
            if (matchIsReady) {
                thread(start = true) {
                    val postMessage = RichMessage().apply {
                        text = "Go go go! Meet ${actualPlayers} by the kicker."
                        channel = interactiveMessage.user.get().id
                    }
                    try {
                        val headers = HttpHeaders()
                        headers.contentType = MediaType.APPLICATION_JSON
                        headers.setBearerAuth("")
                        val request = HttpEntity(postMessage.encodedMessage(), headers)
                        restTemplate.postForEntity(slackApiEndpoints.chat().postMessage(), request, String::class.java)
                    } catch (e: RestClientException) {
                        logger.error("couldn't send message to slack", e)
                    }
                }
            }
            return encodedMessage
        }
        return null
    }

    @GetMapping("/slack/auth/redirect")
    fun redirect(@RequestParam("code") code: String, @RequestParam("state") state: String): String {
        print("Test")
        return "Installed"
    }
}