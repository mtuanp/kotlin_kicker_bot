package de.kicker.bot.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import de.kicker.bot.service.KickerMatchService
import de.kicker.bot.service.SlackMessageService
import de.kicker.bot.slack.model.InteractiveMessage
import de.kicker.bot.slack.model.SlackCommandRequest
import me.ramswaroop.jbot.core.slack.models.RichMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
class KickerBotSlackController {
    val logger: Logger = LoggerFactory.getLogger(KickerBotSlackController::class.java)

    @Autowired
    lateinit var kickerMatchService: KickerMatchService

    @Autowired
    lateinit var slackMessageService: SlackMessageService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @PostMapping("/slack/kickergame", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun startKickerGame(@ModelAttribute slackCommandRequest: SlackCommandRequest): String {
        CompletableFuture.runAsync {
            val uuid = kickerMatchService.createKickerGame(slackCommandRequest.team_id, slackCommandRequest.user_id)
            val players = kickerMatchService.listMatchPlayers(uuid)
            slackMessageService.postInitialMessageToChannel(uuid, players, slackCommandRequest.response_url)
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
            val actualPlayers = kickerMatchService.listMatchPlayers(uuid)
            val actualPlayersAsString = kickerMatchService.listMatchPlayers(uuid).joinToString { "<@${it}>" }
            val encodedMessage = slackMessageService.prepareAddPlayerMessage(interactiveMessage, actualPlayersAsString, matchIsReady)
            if (matchIsReady) {
                CompletableFuture.runAsync {
                    actualPlayers.parallelStream().forEach { playerId ->
                        slackMessageService.postUserGoMessageNotification(interactiveMessage.team.get().id, playerId, actualPlayersAsString)
                    }
                }
            }
            return encodedMessage
        }
        return null
    }

    @GetMapping("/slack/auth/redirect")
    fun redirect(@RequestParam("code") code: String?, @RequestParam("state") state: String?): ResponseEntity<String> {
        if (code != null && slackMessageService.postAuthTokenVerifierAndSave(code)) {
            return ResponseEntity.ok("OK")
        }
        return ResponseEntity.badRequest().body("NOT OK")
    }
}