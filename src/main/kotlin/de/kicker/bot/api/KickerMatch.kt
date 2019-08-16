package de.kicker.bot.api

import java.util.concurrent.ConcurrentLinkedQueue

data class KickerMatch(val teamId: String, private val players: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()) {
    val MAX_PLAYERS = 4

    fun addPlayer(playerId: String): Boolean {
        if (players.size == MAX_PLAYERS) {
            return false
        }
        if (players.contains(playerId)) {
            return false
        }
        return players.add(playerId)
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