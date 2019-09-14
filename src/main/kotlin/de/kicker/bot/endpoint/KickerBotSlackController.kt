package de.kicker.bot.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import de.kicker.bot.api.MatchInteraction
import de.kicker.bot.service.KickerBotSlackService
import de.kicker.bot.slack.model.Action
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

/**
 * Rest controller, for all slack relevant api. It can start match and handle joining or leaving.
 */
@RestController
class KickerBotSlackController {
    val logger: Logger = LoggerFactory.getLogger(KickerBotSlackController::class.java)

    @Autowired
    lateinit var kickerBotSlackService: KickerBotSlackService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    /**
     * Start a kicker match with the slack command. The Response is returned asynchronous.
     */
    @PostMapping("/slack/kickergame", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun startKickerGame(@ModelAttribute slackCommandRequest: SlackCommandRequest): String {
        CompletableFuture.runAsync {
            kickerBotSlackService.startKickerGame(slackCommandRequest.team_id, slackCommandRequest.user_id, slackCommandRequest.response_url)
            logger.debug("Game created")
        }
        return ""
    }

    /**
     * Callback api for joining, leaving or cancel games.
     */
    @PostMapping("/slack/receive", produces = ["application/json"])
    fun callbackKickerGame(@RequestParam("payload") payload: String): RichMessage? {
        try {
            val interactiveMessage = objectMapper.readValue(payload, InteractiveMessage::class.java)
            val uuid = interactiveMessage.callbackId
            val teamId = interactiveMessage.team.id
            val userId = interactiveMessage.user.id
            return when (interactiveMessage.actions.map(Action::value).map { MatchInteraction.valueOf(it) }.firstOrNull()) {
                MatchInteraction.JOIN_MATCH -> kickerBotSlackService.joinMatch(uuid, teamId, userId, interactiveMessage.originalMessage)
                MatchInteraction.LEAVE_MATCH -> kickerBotSlackService.leaveMatch(uuid, teamId, userId, interactiveMessage.originalMessage)
                MatchInteraction.CANCEL_MATCH -> kickerBotSlackService.cancelMatch(uuid, teamId, userId)
                else -> null
            }
        } finally {
            logger.debug("Game event executed")
        }
    }

    @GetMapping("/slack/auth/redirect")
    fun oauthRedirect(@RequestParam("code") code: String?, @RequestParam("state") state: String?): ResponseEntity<String> {
        logger.info("OAuth process started")
        if (code != null && kickerBotSlackService.receiveAccessToken(code)) {
            logger.debug("OAuth process successfully")
            return ResponseEntity.ok("OK")
        }
        logger.debug("OAuth process failed")
        return ResponseEntity.badRequest().body("NOT OK")
    }
}