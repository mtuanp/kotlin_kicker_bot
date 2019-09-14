package de.kicker.bot.service

import de.kicker.bot.api.ErrorCode
import de.kicker.bot.slack.model.OriginMessage
import me.ramswaroop.jbot.core.slack.models.RichMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * Business logic service
 */
@Service
class KickerBotSlackService {
    val logger: Logger = LoggerFactory.getLogger(KickerBotSlackService::class.java)

    @Autowired
    lateinit var kickerMatchService: KickerMatchService

    @Autowired
    lateinit var slackMessageService: SlackMessageService

    /**
     * Starting a kicker game.
     */
    fun startKickerGame(teamId: String, userId: String, responseUrl: String) {
        val uuid = kickerMatchService.createKickerGame(teamId, userId)
        val players = kickerMatchService.listMatchPlayers(uuid)
        slackMessageService.postInitialMessageToChannel(uuid, players, responseUrl)
    }

    /**
     * Joining the kicker game.
     */
    fun joinMatch(uuid: String, teamId: String, userId: String, originMessage: OriginMessage): RichMessage? {
        val joinResult = kickerMatchService.addPlayerToMatch(uuid, teamId, userId)
        if (joinResult.success) {
            val matchIsReady = kickerMatchService.matchIsReady(uuid)
            val actualPlayers = kickerMatchService.listMatchPlayers(uuid)
            val actualPlayersAsString = actualPlayers.joinToString { "<@${it}>" }
            val returnMessage = slackMessageService.createAddPlayerMessage(originMessage, actualPlayersAsString, matchIsReady)
            if (matchIsReady) {
                CompletableFuture.runAsync {
                    actualPlayers.parallelStream().forEach { playerId ->
                        slackMessageService.postUserGoMessageNotification(teamId, playerId, actualPlayersAsString)
                    }
                }
            }
            return returnMessage
        }
        return when (joinResult.errorCode) {
            ErrorCode.MATCH_NOT_FOUND -> slackMessageService.createMatchTimeoutMessage()
            ErrorCode.MATCH_ADD_NOT_POSSIBLE,
            ErrorCode.MATCH_MAX_PLAYERS_REACHED,
            ErrorCode.TEAM_ID_MISMATCHED -> {
                logger.error("Unexpected state for this game. ErrorCode: ${joinResult.errorCode}")
                null
            }
            else -> null
        }
    }

    fun leaveMatch(uuid: String, teamId: String, userId: String, originMessage: OriginMessage): RichMessage? {
        val leaveResult = kickerMatchService.removePlayerFromMatch(uuid, teamId, userId)
        if (leaveResult.success) {
            val matchIsReady = kickerMatchService.matchIsReady(uuid)
            val actualPlayersAsString = kickerMatchService.listMatchPlayers(uuid).joinToString { "<@${it}>" }
            return slackMessageService.createAddPlayerMessage(originMessage, actualPlayersAsString, matchIsReady)
        }
        return when (leaveResult.errorCode) {
            ErrorCode.MATCH_NOT_FOUND -> slackMessageService.createMatchTimeoutMessage()
            ErrorCode.MATCH_MAX_PLAYERS_REACHED,
            ErrorCode.TEAM_ID_MISMATCHED -> {
                logger.error("Unexpected state for this game. ErrorCode: ${leaveResult.errorCode}")
                null
            }
            else -> null
        }
    }

    fun cancelMatch(uuid: String, teamId: String, userId: String): RichMessage? {
        val cancelResult = kickerMatchService.cancelMatch(uuid, teamId, userId)
        if (cancelResult.success) {
            return slackMessageService.createMatchCanceledMessage()
        }
        return when (cancelResult.errorCode) {
            ErrorCode.MATCH_NOT_FOUND -> slackMessageService.createMatchTimeoutMessage()
            else -> null
        }
    }

    fun receiveAccessToken(code: String): Boolean {
        return slackMessageService.postAuthTokenVerifierAndSave(code)
    }
}