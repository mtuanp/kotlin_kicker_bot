package de.kicker.bot.service

import com.google.common.cache.Cache
import de.kicker.bot.api.ErrorCode
import de.kicker.bot.api.KickerMatch
import de.kicker.bot.api.Result
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
/**
 * Kicker match service for all kicker match relevant oprations like creating and adding player and list all players
 */
class KickerMatchService {
    @Autowired
    lateinit var kickerMatchCache: Cache<String, KickerMatch>

    /**
     * Create a kicker match and add the initial player to the match. Return a uuid string as match key.
     */
    fun createKickerGame(teamId: String, userId: String): String {
        val uuid = UUID.randomUUID().toString()
        kickerMatchCache.put(uuid, KickerMatch(teamId).apply {
            addPlayer(userId)
        })
        return uuid
    }

    /**
     * Function for adding player to a match with given uuid. It also check for the correct team id.
     */
    fun addPlayerToMatch(uuid: String, teamId: String, userId: String): Result {
        val kickerMatch = kickerMatchCache.getIfPresent(uuid) ?: return Result.error(ErrorCode.MATCH_NOT_FOUND)
        return if (kickerMatch.teamId == teamId) {
            kickerMatch.addPlayer(userId)
        } else {
            Result.error(ErrorCode.TEAM_ID_MISMATCHED)
        }
    }

    /**
     * Function for removing player from a match with given uuid. It also check for the correct team id.
     */
    fun removePlayerFromMatch(uuid: String, teamId: String, userId: String): Result {
        val kickerMatch = kickerMatchCache.getIfPresent(uuid) ?: return Result.error(ErrorCode.MATCH_NOT_FOUND)
        return if (kickerMatch.teamId == teamId) {
            kickerMatch.removePlayer(userId)
        } else {
            Result.error(ErrorCode.TEAM_ID_MISMATCHED)
        }
    }

    /**
     * Cancel the match. Remove it from the cache.
     */
    fun cancelMatch(uuid: String, teamId: String, userId: String): Result {
        val kickerMatch = kickerMatchCache.getIfPresent(uuid) ?: return Result.error(ErrorCode.MATCH_NOT_FOUND)
        return if (kickerMatch.teamId == teamId) {
            if( kickerMatch.hasPlayer(userId)) {
                kickerMatchCache.invalidate(uuid)
                Result.success()
            } else {
                Result.error(ErrorCode.MATCH_DID_NOT_CONTAINS_PLAYER)
            }
        } else {
            Result.error(ErrorCode.TEAM_ID_MISMATCHED)
        }
    }

    /**
     * List all players of the kicker match as collection.
     */
    fun listMatchPlayers(uuid: String): Collection<String> {
        val kickerMatch = kickerMatchCache.getIfPresent(uuid)
        return kickerMatch!!.listPlayers()
    }

    /**
     * Return true if the kicker match is ready for play.
     */
    fun matchIsReady(uuid: String): Boolean {
        val kickerMatch = kickerMatchCache.getIfPresent(uuid)
        return kickerMatch!!.matchIsReady()
    }

}