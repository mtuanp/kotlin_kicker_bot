package de.kicker.bot.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.kicker.bot.api.ErrorCode
import de.kicker.bot.api.KickerMatch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KickerMatchServiceTest {
    val kickerCache: Cache<String, KickerMatch> = CacheBuilder.newBuilder().maximumSize(10).build();
    val kickerMatchService = KickerMatchService().apply { kickerMatchCache = kickerCache }

    @BeforeEach
    fun setUp() {
        kickerCache.invalidateAll()
    }

    @Test
    fun createKickerGame() {
        val uuid = kickerMatchService.createKickerGame("TeamA", "Player1")
        assertNotNull(uuid)
        assertTrue(uuid.isNotEmpty())
        assertTrue(kickerCache.size() == 1L)
        kickerCache.getIfPresent(uuid)!!.apply {
            assertTrue(teamId == "TeamA")
            assertTrue(listPlayers().size == 1)
            assertTrue(listPlayers().contains("Player1"))
        }
    }

    @Test
    fun createKickerGameSecondTest() {
        kickerMatchService.createKickerGame("TeamA", "Player1")
        val uuid = kickerMatchService.createKickerGame("TeamA", "Player1")
        assertNotNull(uuid)
        assertTrue(uuid.isNotEmpty())
        assertTrue(kickerCache.size() == 2L)
        kickerCache.getIfPresent(uuid)!!.apply {
            assertTrue(teamId == "TeamA")
            assertTrue(listPlayers().size == 1)
            assertTrue(listPlayers().contains("Player1"))
        }
    }

    @Test
    fun addPlayerToMatchNotFoundMatch() {
        val result = kickerMatchService.addPlayerToMatch("NotFoundUUID", "TeamA", "Player2")
        assertFalse(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.MATCH_NOT_FOUND)
        assertEquals(0, kickerCache.size())
    }

    @Test
    fun removingPlayerFromMatchNotFoundMatch() {
        val result = kickerMatchService.removePlayerFromMatch("NotFoundUUID", "TeamA", "Player2")
        assertFalse(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.MATCH_NOT_FOUND)
        assertEquals(0, kickerCache.size())
    }

    @Test
    fun addPlayerToMatchNotFoundMatch2() {
        kickerCache.put("MyUUID", KickerMatch("TeamA"))
        val result = kickerMatchService.addPlayerToMatch("NotFoundUUID", "TeamA", "Player2")
        assertFalse(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.MATCH_NOT_FOUND)
        assertEquals(1, kickerCache.size())
    }

    @Test
    fun removePlayerFromMatchNotFoundMatch2() {
        kickerCache.put("MyUUID", KickerMatch("TeamA"))
        val result = kickerMatchService.removePlayerFromMatch("NotFoundUUID", "TeamA", "Player2")
        assertFalse(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.MATCH_NOT_FOUND)
        assertEquals(1, kickerCache.size())
    }

    @Test
    fun addPlayerToMatchSuccess() {
        kickerCache.put("MyUUID", KickerMatch("TeamA"))
        val result = kickerMatchService.addPlayerToMatch("MyUUID", "TeamA", "Player2")
        assertTrue(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.NOTHING)
        assertEquals(1, kickerCache.size())
        assertEquals(1, kickerCache.getIfPresent("MyUUID")?.listPlayers()?.size)
    }

    @Test
    fun removePlayerFromMatchSuccess() {
        kickerCache.put("MyUUID", KickerMatch("TeamA").apply { addPlayer("Player2") })
        val result = kickerMatchService.removePlayerFromMatch("MyUUID", "TeamA", "Player2")
        assertTrue(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.NOTHING)
        assertEquals(1, kickerCache.size())
        assertEquals(0, kickerCache.getIfPresent("MyUUID")?.listPlayers()?.size)
    }

    @Test
    fun addPlayerToMatchWithIncorrectTeam() {
        kickerCache.put("MyUUID", KickerMatch("TeamR"))
        val result = kickerMatchService.addPlayerToMatch("MyUUID", "TeamA", "Player2")
        assertFalse(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.TEAM_ID_MISMATCHED)
        assertEquals(1, kickerCache.size())
        assertEquals(0, kickerCache.getIfPresent("MyUUID")?.listPlayers()?.size)
    }

    @Test
    fun removePlayerFromMatchWithIncorrectTeam() {
        kickerCache.put("MyUUID", KickerMatch("TeamR"))
        val result = kickerMatchService.removePlayerFromMatch("MyUUID", "TeamA", "Player2")
        assertFalse(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.TEAM_ID_MISMATCHED)
        assertEquals(1, kickerCache.size())
        assertEquals(0, kickerCache.getIfPresent("MyUUID")?.listPlayers()?.size)
    }

    @Test
    fun addPlayerToMatchSuccess2() {
        kickerCache.put("MyUUID", KickerMatch("TeamA").apply { addPlayer("Player1") })
        val result = kickerMatchService.addPlayerToMatch("MyUUID", "TeamA", "Player2")
        assertTrue(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.NOTHING)
        assertEquals(1, kickerCache.size())
        assertEquals(2, kickerCache.getIfPresent("MyUUID")?.listPlayers()?.size)
    }

    @Test
    fun add2SamePlayer() {
        kickerCache.put("MyUUID", KickerMatch("TeamA").apply { addPlayer("Player1") })
        val result = kickerMatchService.addPlayerToMatch("MyUUID", "TeamA", "Player1")
        assertFalse(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.MATCH_CONTAINS_PLAYER)
        assertEquals(1, kickerCache.size())
        assertEquals(1, kickerCache.getIfPresent("MyUUID")?.listPlayers()?.size)
    }

    @Test
    fun add3PlayerShouldWork() {
        kickerCache.put("MyUUID", KickerMatch("TeamA").apply {
            addPlayer("Player1")
            addPlayer("Player2")
        })
        val result = kickerMatchService.addPlayerToMatch("MyUUID", "TeamA", "Player3")
        assertTrue(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.NOTHING)
        assertEquals(1, kickerCache.size())
        assertEquals(3, kickerCache.getIfPresent("MyUUID")?.listPlayers()?.size)
    }

    @Test
    fun add4PlayerShouldWork() {
        kickerCache.put("MyUUID", KickerMatch("TeamA").apply {
            addPlayer("Player1")
            addPlayer("Player2")
            addPlayer("Player3")
        })
        val result = kickerMatchService.addPlayerToMatch("MyUUID", "TeamA", "Player4")
        assertTrue(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.NOTHING)
        assertEquals(1, kickerCache.size())
        assertEquals(4, kickerCache.getIfPresent("MyUUID")?.listPlayers()?.size)
    }

    @Test
    fun add5PlayerShouldNotWork() {
        kickerCache.put("MyUUID", KickerMatch("TeamA").apply {
            addPlayer("Player1")
            addPlayer("Player2")
            addPlayer("Player3")
            addPlayer("Player4")
        })
        val result = kickerMatchService.addPlayerToMatch("MyUUID", "TeamA", "Player5")
        assertFalse(result.success)
        assertThat(result.errorCode).isEqualTo(ErrorCode.MATCH_MAX_PLAYERS_REACHED)
        assertEquals(1, kickerCache.size())
        assertEquals(4, kickerCache.getIfPresent("MyUUID")?.listPlayers()?.size)
    }

    @Test
    fun list4PlayerShouldWorkAsExpected() {
        kickerCache.put("MyUUID", KickerMatch("TeamA").apply {
            addPlayer("Player1")
            addPlayer("Player2")
            addPlayer("Player3")
            addPlayer("Player4")
        })
        val players = kickerMatchService.listMatchPlayers("MyUUID")
        assertTrue(players.containsAll(arrayListOf("Player1", "Player2", "Player3", "Player4")))
        assertTrue(players.size == 4)
    }

    @Test
    fun list3PlayerShouldWorkAsExpected() {
        kickerCache.put("MyUUID", KickerMatch("TeamA").apply {
            addPlayer("Player1")
            addPlayer("Player2")
            addPlayer("Player3")
        })
        val players = kickerMatchService.listMatchPlayers("MyUUID")
        assertTrue(players.containsAll(arrayListOf("Player1", "Player2", "Player3")))
        assertTrue(players.size == 3)
    }

    @Test
    fun matchIsReadyWhen4PlayersAdded() {
        kickerCache.put("MyUUID", KickerMatch("TeamA").apply {
            addPlayer("Player1")
            addPlayer("Player2")
            addPlayer("Player3")
            addPlayer("Player4")
        })
        val ready = kickerMatchService.matchIsReady("MyUUID")
        assertTrue(ready)
    }

    @Test
    fun matchIsNotReadyWhen3PlayersAdded() {
        kickerCache.put("MyUUID", KickerMatch("TeamA").apply {
            addPlayer("Player1")
            addPlayer("Player2")
            addPlayer("Player3")
        })
        val ready = kickerMatchService.matchIsReady("MyUUID")
        assertFalse(ready)
    }
}