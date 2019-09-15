package de.kicker.bot.api

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

data class KickerMatch(val teamId: String, private val players: Queue<String> = ConcurrentLinkedQueue()) {

    companion object {
        const val maxPlayers = 4
    }

    fun addPlayer(playerId: String): Result {
        if (players.size == Companion.maxPlayers) {
            return Result.error(ErrorCode.MATCH_MAX_PLAYERS_REACHED)
        }
        if (players.contains(playerId)) {
            return Result.error(ErrorCode.MATCH_CONTAINS_PLAYER)
        }
        return Result.of(players.add(playerId), ErrorCode.MATCH_ADD_NOT_POSSIBLE)
    }

    fun removePlayer(playerId: String): Result {
        return Result.of(players.remove(playerId), ErrorCode.MATCH_DID_NOT_CONTAINS_PLAYER)
    }

    fun listPlayers(): Collection<String> {
        return players
    }

    fun hasPlayer(playerId: String): Boolean {
        return players.contains(playerId)
    }

    fun matchIsReady(): Boolean {
        return players.size == maxPlayers
    }

}