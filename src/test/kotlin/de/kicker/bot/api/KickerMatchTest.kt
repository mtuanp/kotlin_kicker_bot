package de.kicker.bot.api

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

internal class KickerMatchTest {

    @Test
    fun addPlayer() {

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

       // assertThat(match.listPlayers()).contains("Player 1", "Player 2", "Player 3")
        //asse(true, match.hasPlayer("Player 1"))
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