package de.kicker.bot.service

import com.google.common.cache.Cache
import de.kicker.bot.api.KickerMatch
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
    fun addPlayerToMatch(uuid: String, teamId: String, userId: String): Boolean {
        val kickerMatch = kickerMatchCache.getIfPresent(uuid)
        if (kickerMatch?.teamId == teamId) {
            return kickerMatch.addPlayer(userId)
        }
        return false
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