package de.kicker.bot.service

import de.kicker.bot.api.ErrorCode
import de.kicker.bot.api.KickerMatch
import de.kicker.bot.slack.model.ActionableAttachment
import de.kicker.bot.util.SlackUserIdSplitter
import me.ramswaroop.jbot.core.slack.models.RichMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

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
    fun startKickerGame(teamId: String, userId: String, args: String, responseUrl: String, async: Boolean = true) {
        val uuid = kickerMatchService.createKickerGame(teamId, userId)
        val matchIsReady = when {
            args.isNotEmpty() -> {
                SlackUserIdSplitter.toIdList(args)
                        .take(KickerMatch.maxPlayers - 1)
                        .forEach { playerId -> kickerMatchService.addPlayerToMatch(uuid, teamId, playerId) }
                kickerMatchService.matchIsReady(uuid)
            }
            else -> false
        }
        val players = kickerMatchService.listMatchPlayers(uuid)
        val actualPlayersAsString = players.joinToString { "<@${it}>" }
        if (matchIsReady.not()) {
            slackMessageService.postInitialMessageToChannel(uuid, actualPlayersAsString, responseUrl)
        } else {
            slackMessageService.postInitialReadyMatchMessageToChannel(actualPlayersAsString, responseUrl)
            CompletableFuture.runAsync {
                players.parallelStream().forEach { playerId ->
                    slackMessageService.postUserGoMessageNotification(teamId, playerId, actualPlayersAsString)
                }
            }.also {
                if (async.not()) {
                    it.get()
                }
            }
        }

    }

    /**
     * Joining the kicker game.
     */
    fun joinMatch(uuid: String, teamId: String, userId: String, channelId: String, messageTs: String, originAttachment: ActionableAttachment, async: Boolean = true): RichMessage? {
        val joinResult = kickerMatchService.addPlayerToMatch(uuid, teamId, userId)
        if (joinResult.success) {
            val matchIsReady = kickerMatchService.matchIsReady(uuid)
            val actualPlayers = kickerMatchService.listMatchPlayers(uuid)
            val actualPlayersAsString = actualPlayers.joinToString { "<@${it}>" }
            val returnMessage = slackMessageService.createActiveMatchMessage(originAttachment, actualPlayersAsString, matchIsReady)
            if (matchIsReady) {
                CompletableFuture.runAsync {
                    actualPlayers.parallelStream().forEach { playerId ->
                        slackMessageService.postUserGoMessageNotification(teamId, playerId, actualPlayersAsString)
                    }
                    if (async) TimeUnit.MINUTES.sleep(2)
                    slackMessageService.deleteMessage(teamId, channelId, messageTs)
                }.also {
                    if (async.not()) {
                        it.get()
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

    /**
     * leve the kicker match.
     */
    fun leaveMatch(uuid: String, teamId: String, userId: String, originActionableAttachment: ActionableAttachment): RichMessage? {
        val leaveResult = kickerMatchService.removePlayerFromMatch(uuid, teamId, userId)
        if (leaveResult.success) {
            val matchIsReady = kickerMatchService.matchIsReady(uuid)
            val actualPlayersAsString = kickerMatchService.listMatchPlayers(uuid).joinToString { "<@${it}>" }
            return slackMessageService.createActiveMatchMessage(originActionableAttachment, actualPlayersAsString, matchIsReady)
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

    /**
     * cancel the kicker match
     */
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

    /**
     * receive a code for access token from slack.
     */
    fun receiveAccessToken(code: String): Boolean {
        return slackMessageService.postAuthTokenVerifierAndSave(code)
    }
}