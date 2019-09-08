package de.kicker.bot.api

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

internal class KickerMatchTest {

    @Test
    fun addPlayerSuccess() {
        val players : Queue<String> = LinkedList()
        val match = KickerMatch("Team Rocket", players)
        val result = match.addPlayer("Player 1")
        assertThat(result.success).isTrue()
        assertThat(result.errorCode).isEqualTo(ErrorCode.NOTHING)
    }

    @Test
    fun addPlayerMaxPlayersReached() {
        val players : Queue<String> = LinkedList()
        players.apply {
            add("Player 1")
            add("Player 2")
            add("Player 3")
            add("Player 4")
        }
        val match = KickerMatch("Team Rocket", players)
        val result = match.addPlayer("Player 5")
        assertThat(result.success).isFalse()
        assertThat(result.errorCode).isEqualTo(ErrorCode.MATCH_MAX_PLAYERS_REACHED)
    }

    @Test
    fun addAlreadyInMatchPlayer() {
        val players : Queue<String> = LinkedList()
        players.apply {
            add("Player 1")
        }
        val match = KickerMatch("Team Rocket", players)
        val result = match.addPlayer("Player 1")
        assertThat(result.success).isFalse()
        assertThat(result.errorCode).isEqualTo(ErrorCode.MATCH_CONTAINS_PLAYER)
    }

    @Test
    fun addPlayerNotPossible() {
        val players : Queue<String> =  mockk()
        every { players.size } returns 0
        every { players.contains("Player 2") } returns false
        every { players.add("Player 2") } returns false

        val match = KickerMatch("Team Rocket", players )
        val result = match.addPlayer("Player 2")
        assertThat(result.success).isFalse()
        assertThat(result.errorCode).isEqualTo(ErrorCode.MATCH_ADD_NOT_POSSIBLE)
    }

    @Test
    fun removeNotPossible() {
        val players : Queue<String> = LinkedList()
        players.apply {
            add("Player 1")
        }

        val match = KickerMatch("Team Rocket", players )
        val result = match.removePlayer("Player 2")
        assertThat(result.success).isFalse()
        assertThat(result.errorCode).isEqualTo(ErrorCode.MATCH_DID_NOT_CONTAINS_PLAYER)
    }

    @Test
    fun removePlayer() {
        val players : Queue<String> = LinkedList()
        players.apply {
            add("Player 1")
        }

        val match = KickerMatch("Team Rocket", players )
        val result = match.removePlayer("Player 1")
        assertThat(result.success).isTrue()
        assertThat(result.errorCode).isEqualTo(ErrorCode.NOTHING)
    }

    @Test
    fun listPlayers() {
        val players : Queue<String> = LinkedList()
        players.apply {
            add("Player 1")
            add("Player 2")
            add("Player 3")
        }
        val match = KickerMatch("Team Rocket", players)

        assertThat(match.listPlayers()).contains("Player 1", "Player 2", "Player 3")
    }

    @Test
    fun hasPlayer() {
        val players : Queue<String> = LinkedList()
        players.apply {
            add("Player 1")
            add("Player 2")
            add("Player 3")
        }
        val match = KickerMatch("Team Rocket", players)
        assertEquals(true, match.hasPlayer("Player 1"))
    }

    @Test
    fun hasNotPlayer() {
        val players : Queue<String> = LinkedList()
        players.apply {
            add("Player 1")
            add("Player 2")
            add("Player 3")
        }
        val match = KickerMatch("Team Rocket", players)
        assertEquals(false, match.hasPlayer("Player 4"))
    }

    @Test
    fun testingNotReadyMatchIfItIsReady() {
        val players : Queue<String> = LinkedList()
        val match = KickerMatch("Team Rocket", players)
        assertEquals(false, match.matchIsReady())
    }

    @Test
    fun testingReadyMatchIfItIsReady() {
        val players : Queue<String> = LinkedList()
        players.apply {
            add("Player 1")
            add("Player 2")
            add("Player 3")
            add("Player 4")
        }
        val match = KickerMatch("Team Rocket", players)
        assertEquals(true, match.matchIsReady())
    }

    @Test
    fun getTeamId() {
        val match = KickerMatch("Team Rocket")
        assertEquals(match.teamId, "Team Rocket")
    }
}