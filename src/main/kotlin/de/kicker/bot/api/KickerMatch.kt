package de.kicker.bot.api

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

data class KickerMatch(val teamId: String, private val players: Queue<String> = ConcurrentLinkedQueue()) {
    internal val MAX_PLAYERS = 4

    fun addPlayer(playerId: String): Pair<Boolean, ErrorCode> {
        if (players.size == MAX_PLAYERS) {
            return Pair(false, ErrorCode.MATCH_HAS_MAX_PLAYER)
        }
        if (players.contains(playerId)) {
            return Pair(false, ErrorCode.MATCH_CONTAINS_PLAYER)
        }
        val result = players.add(playerId)
        return Pair(result, if (result) ErrorCode.NOTHING else ErrorCode.MATCH_ADD_NOT_POSSIBLE)
    }

    fun listPlayers(): Collection<String> {
        return players
    }

    fun hasPlayer(playerId: String): Boolean {
        return players.contains(playerId)
    }

    fun matchIsReady(): Boolean {
        return players.size == MAX_PLAYERS
    }

}