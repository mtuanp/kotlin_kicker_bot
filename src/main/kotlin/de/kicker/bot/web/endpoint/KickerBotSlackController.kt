package de.kicker.bot.web.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import de.kicker.bot.web.api.Action
import de.kicker.bot.web.api.ActionableAttachment
import de.kicker.bot.web.api.InteractiveMessage
import de.kicker.bot.web.api.SlackCommandRequest
import me.ramswaroop.jbot.core.slack.models.RichMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.*
import kotlin.concurrent.thread


@RestController
class KickerBotSlackController {
    val logger: Logger = LoggerFactory.getLogger(KickerBotSlackController::class.java)

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @PostMapping("/slack/kickergame", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun startKickerGame(@ModelAttribute slackCommandRequest: SlackCommandRequest): String {
        thread(start = true) {
            val uuid = UUID.randomUUID()
            val richMessage = RichMessage("Hello, it's time for a Kicker game ⚽").apply {
                responseType = "in_channel"
                attachments = arrayOf(ActionableAttachment().apply {
                    text = "Actual players: <@${slackCommandRequest.user_id}>"
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
                        value = uuid.toString()
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
    fun callbackKickerGame(@RequestParam("payload") payload: String): RichMessage {
        val interactiveMessage = objectMapper.readValue(payload, InteractiveMessage::class.java)

        val richMessage = RichMessage(interactiveMessage.original_message.get().text).apply {
            responseType = "in_channel"
            attachments = arrayOf(
                    interactiveMessage.original_message.get().attachments[0].also { it.text = it.text + ", <@${interactiveMessage.user.get().id}>" },
                    interactiveMessage.original_message.get().attachments[1]
            )
        }
        // Always remember to send the encoded message to Slack
        val encodedMessage = richMessage.encodedMessage()
        return encodedMessage
    }
}