package de.kicker.bot.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.kicker.bot.api.KickerMatch
import de.kicker.bot.slack.model.ActionableAttachment
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KickerBotSlackServiceTest {

    private lateinit var kickerBotSlackService: KickerBotSlackService
    private lateinit var myKickerMatchCache: Cache<String, KickerMatch>
    private lateinit var kickerMatchServiceSpy: KickerMatchService
    private lateinit var slackMessageServiceMock: SlackMessageService

    @BeforeEach
    fun setup() {
        myKickerMatchCache = CacheBuilder.newBuilder().maximumSize(10).build()
        kickerMatchServiceSpy = spyk(KickerMatchService().apply {
            kickerMatchCache = myKickerMatchCache
        })
        slackMessageServiceMock = mockk(relaxed = true)
        kickerBotSlackService = KickerBotSlackService().apply {
            kickerMatchService = kickerMatchServiceSpy
            slackMessageService = slackMessageServiceMock
        }
    }

    @Test
    fun startKickerGame() {
        val teamId = "TeamA"
        val userId = "User1"
        val args = ""
        val responseUrl = "ReturnUrl"
        kickerBotSlackService.startKickerGame(teamId, userId, args, responseUrl)

        verify(exactly = 1) { kickerMatchServiceSpy.createKickerGame(teamId, userId) }
        verify(exactly = 0) { kickerMatchServiceSpy.matchIsReady(any()) }
        verify(exactly = 1) { kickerMatchServiceSpy.listMatchPlayers(any()) }
        verify(exactly = 1) { slackMessageServiceMock.postInitialMessageToChannel(any(), any(), responseUrl) }
        verify(exactly = 0) { slackMessageServiceMock.postInitialReadyMatchMessageToChannel(any(), responseUrl) }
    }

    @Test
    fun `startKickerGameWith 2 Players`() {
        val teamId = "TeamA"
        val userId = "User1"
        val args = "<@User2|User 2>"
        val responseUrl = "ReturnUrl"
        kickerBotSlackService.startKickerGame(teamId, userId, args, responseUrl)

        verify(exactly = 1) { kickerMatchServiceSpy.createKickerGame(teamId, userId) }
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(any(), teamId, "User2") }
        verify(exactly = 1) { kickerMatchServiceSpy.matchIsReady(any()) }
        verify(exactly = 1) { kickerMatchServiceSpy.listMatchPlayers(any()) }
        verify(exactly = 1) { slackMessageServiceMock.postInitialMessageToChannel(any(), any(), responseUrl) }
        verify(exactly = 0) { slackMessageServiceMock.postInitialReadyMatchMessageToChannel(any(), responseUrl) }
    }

    @Test
    fun `startKickerGameWith 4 Players`() {
        val teamId = "TeamA"
        val userId = "User1"
        val args = "<@User2|User 2> <@User3|User 3> <@User4|User 4>"
        val responseUrl = "ReturnUrl"
        kickerBotSlackService.startKickerGame(teamId, userId, args, responseUrl, false)

        verify(exactly = 1) { kickerMatchServiceSpy.createKickerGame(teamId, userId) }
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(any(), teamId, "User2") }
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(any(), teamId, "User3") }
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(any(), teamId, "User4") }
        verify(exactly = 1) { kickerMatchServiceSpy.matchIsReady(any()) }
        verify(exactly = 1) { kickerMatchServiceSpy.listMatchPlayers(any()) }
        verify(exactly = 0) { slackMessageServiceMock.postInitialMessageToChannel(any(), any(), responseUrl) }
        verify(exactly = 1) { slackMessageServiceMock.postInitialReadyMatchMessageToChannel(any(), responseUrl) }
        verify(exactly = 4) { slackMessageServiceMock.postUserGoMessageNotification(teamId, any(), any()) }
    }

    @Test
    fun `startKickerGameWith 6 Players`() {
        val teamId = "TeamA"
        val userId = "User1"
        val args = "<@User2|User 2> <@User3|User 3> <@User4|User 4>  <@User5|User 5>  <@User6|User 6>"
        val responseUrl = "ReturnUrl"
        kickerBotSlackService.startKickerGame(teamId, userId, args, responseUrl, false)

        verify(exactly = 1) { kickerMatchServiceSpy.createKickerGame(teamId, userId) }
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(any(), teamId, "User2") }
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(any(), teamId, "User3") }
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(any(), teamId, "User4") }
        verify(exactly = 0) { kickerMatchServiceSpy.addPlayerToMatch(any(), teamId, "User5") }
        verify(exactly = 0) { kickerMatchServiceSpy.addPlayerToMatch(any(), teamId, "User6") }
        verify(exactly = 1) { kickerMatchServiceSpy.matchIsReady(any()) }
        verify(exactly = 1) { kickerMatchServiceSpy.listMatchPlayers(any()) }
        verify(exactly = 0) { slackMessageServiceMock.postInitialMessageToChannel(any(), any(), responseUrl) }
        verify(exactly = 1) { slackMessageServiceMock.postInitialReadyMatchMessageToChannel(any(), responseUrl) }
        verify(exactly = 4) { slackMessageServiceMock.postUserGoMessageNotification(teamId, any(), any()) }
    }

    @Test
    fun joinMatch() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val userId2 = "User2"
        val originAttachment = ActionableAttachment()
        val givenChannelId = "ChannelX"
        val givenMessageTs = "1234545678789"
        myKickerMatchCache.put(uuid, KickerMatch(teamId).apply { addPlayer(userId) })
        val returnMessage = kickerBotSlackService.joinMatch(uuid, teamId, userId2, givenChannelId, givenMessageTs, originAttachment)

        assertThat(returnMessage).isNotNull
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(uuid, teamId, userId2) }
        verify(exactly = 1) { kickerMatchServiceSpy.matchIsReady(uuid) }
        verify(exactly = 1) { kickerMatchServiceSpy.listMatchPlayers(uuid) }
        verify(exactly = 1) { slackMessageServiceMock.createActiveMatchMessage(originAttachment, any(), any()) }
        verify(exactly = 0) { slackMessageServiceMock.postUserGoMessageNotification(teamId, any(), any()) }
        verify(exactly = 0) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }

    @Test
    fun `joinMatch with 3 Players`() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val userId2 = "User2"
        val userId3 = "User3"
        val originAttachment = ActionableAttachment()
        val givenChannelId = "ChannelX"
        val givenMessageTs = "1234545678789"
        myKickerMatchCache.put(uuid, KickerMatch(teamId).apply {
            addPlayer(userId)
            addPlayer(userId2)
        })
        val returnMessage = kickerBotSlackService.joinMatch(uuid, teamId, userId3, givenChannelId, givenMessageTs, originAttachment)

        assertThat(returnMessage).isNotNull
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(uuid, teamId, userId3) }
        verify(exactly = 1) { kickerMatchServiceSpy.matchIsReady(uuid) }
        verify(exactly = 1) { kickerMatchServiceSpy.listMatchPlayers(uuid) }
        verify(exactly = 1) { slackMessageServiceMock.createActiveMatchMessage(originAttachment, any(), any()) }
        verify(exactly = 0) { slackMessageServiceMock.postUserGoMessageNotification(teamId, any(), any()) }
        verify(exactly = 0) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }

    @Test
    fun `joinMatch with 4 Players`() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val userId2 = "User2"
        val userId3 = "User3"
        val userId4 = "User4"
        val originAttachment = ActionableAttachment()
        val givenChannelId = "ChannelX"
        val givenMessageTs = "1234545678789"
        myKickerMatchCache.put(uuid, KickerMatch(teamId).apply {
            addPlayer(userId)
            addPlayer(userId2)
            addPlayer(userId3)
        })
        val returnMessage = kickerBotSlackService.joinMatch(uuid, teamId, userId4, givenChannelId, givenMessageTs, originAttachment, false)

        assertThat(returnMessage).isNotNull
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(uuid, teamId, userId4) }
        verify(exactly = 1) { kickerMatchServiceSpy.matchIsReady(uuid) }
        verify(exactly = 1) { kickerMatchServiceSpy.listMatchPlayers(uuid) }
        verify(exactly = 1) { slackMessageServiceMock.createActiveMatchMessage(originAttachment, any(), any()) }
        verify(exactly = 4) { slackMessageServiceMock.postUserGoMessageNotification(teamId, any(), any()) }
        verify(exactly = 1) { slackMessageServiceMock.deleteMessage(teamId, givenChannelId, givenMessageTs) }
        verify(exactly = 0) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }

    @Test
    fun `joinMatch match not found`() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val userId2 = "User2"
        val originAttachment = ActionableAttachment()
        val givenChannelId = "ChannelX"
        val givenMessageTs = "1234545678789"
        myKickerMatchCache.put("UUID2", KickerMatch(teamId).apply {
            addPlayer(userId)
        })
        val returnMessage = kickerBotSlackService.joinMatch(uuid, teamId, userId2, givenChannelId, givenMessageTs, originAttachment, false)

        assertThat(returnMessage).isNotNull
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(uuid, teamId, userId2) }
        verify(exactly = 1) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }

    @Test
    fun `joinMatch teamId not matched`() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val userId2 = "User2"
        val originAttachment = ActionableAttachment()
        val givenChannelId = "ChannelX"
        val givenMessageTs = "1234545678789"
        myKickerMatchCache.put(uuid, KickerMatch("TeamB").apply {
            addPlayer(userId)
        })
        val returnMessage = kickerBotSlackService.joinMatch(uuid, teamId, userId2, givenChannelId, givenMessageTs, originAttachment, false)

        assertThat(returnMessage).isNull()
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(uuid, teamId, userId2) }
    }

    @Test
    fun `joinMatch already joined`() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val originAttachment = ActionableAttachment()
        val givenChannelId = "ChannelX"
        val givenMessageTs = "1234545678789"
        myKickerMatchCache.put(uuid, KickerMatch(teamId).apply {
            addPlayer(userId)
        })
        val returnMessage = kickerBotSlackService.joinMatch(uuid, teamId, userId, givenChannelId, givenMessageTs, originAttachment, false)

        assertThat(returnMessage).isNull()
        verify(exactly = 1) { kickerMatchServiceSpy.addPlayerToMatch(uuid, teamId, userId) }
    }

    @Test
    fun leaveMatch() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val originAttachment = ActionableAttachment()
        myKickerMatchCache.put(uuid, KickerMatch(teamId).apply { addPlayer(userId) })
        val returnMessage = kickerBotSlackService.leaveMatch(uuid, teamId, userId, originAttachment)

        assertThat(returnMessage).isNotNull
        verify(exactly = 1) { kickerMatchServiceSpy.removePlayerFromMatch(uuid, teamId, userId) }
        verify(exactly = 1) { kickerMatchServiceSpy.matchIsReady(uuid) }
        verify(exactly = 1) { kickerMatchServiceSpy.listMatchPlayers(uuid) }
        verify(exactly = 1) { slackMessageServiceMock.createActiveMatchMessage(originAttachment, any(), any()) }
        verify(exactly = 0) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }

    @Test
    fun `leaveMatch no player`() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val originAttachment = ActionableAttachment()
        myKickerMatchCache.put(uuid, KickerMatch(teamId))
        val returnMessage = kickerBotSlackService.leaveMatch(uuid, teamId, userId, originAttachment)

        assertThat(returnMessage).isNull()
        verify(exactly = 1) { kickerMatchServiceSpy.removePlayerFromMatch(uuid, teamId, userId) }
        verify(exactly = 0) { kickerMatchServiceSpy.matchIsReady(uuid) }
        verify(exactly = 0) { kickerMatchServiceSpy.listMatchPlayers(uuid) }
        verify(exactly = 0) { slackMessageServiceMock.createActiveMatchMessage(originAttachment, any(), any()) }
        verify(exactly = 0) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }

    @Test
    fun `leaveMatch timeout`() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val originAttachment = ActionableAttachment()
        val returnMessage = kickerBotSlackService.leaveMatch(uuid, teamId, userId, originAttachment)

        assertThat(returnMessage).isNotNull()
        verify(exactly = 1) { kickerMatchServiceSpy.removePlayerFromMatch(uuid, teamId, userId) }
        verify(exactly = 0) { kickerMatchServiceSpy.matchIsReady(uuid) }
        verify(exactly = 0) { kickerMatchServiceSpy.listMatchPlayers(uuid) }
        verify(exactly = 0) { slackMessageServiceMock.createActiveMatchMessage(originAttachment, any(), any()) }
        verify(exactly = 1) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }

    @Test
    fun `leaveMatch teamId mismatched`() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val originAttachment = ActionableAttachment()
        myKickerMatchCache.put(uuid, KickerMatch("TeamB"))
        val returnMessage = kickerBotSlackService.leaveMatch(uuid, teamId, userId, originAttachment)

        assertThat(returnMessage).isNull()
        verify(exactly = 1) { kickerMatchServiceSpy.removePlayerFromMatch(uuid, teamId, userId) }
        verify(exactly = 0) { kickerMatchServiceSpy.matchIsReady(uuid) }
        verify(exactly = 0) { kickerMatchServiceSpy.listMatchPlayers(uuid) }
        verify(exactly = 0) { slackMessageServiceMock.createActiveMatchMessage(originAttachment, any(), any()) }
        verify(exactly = 0) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }


    @Test
    fun cancelMatch() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        myKickerMatchCache.put(uuid, KickerMatch(teamId).apply { addPlayer(userId) })
        val returnMessage = kickerBotSlackService.cancelMatch(uuid, teamId, userId)

        assertThat(returnMessage).isNotNull
        verify(exactly = 1) { kickerMatchServiceSpy.cancelMatch(uuid, teamId, userId) }
        verify(exactly = 1) { slackMessageServiceMock.createMatchCanceledMessage() }
        verify(exactly = 0) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }

    @Test
    fun `cancelMatch no player`() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        myKickerMatchCache.put(uuid, KickerMatch(teamId).apply { addPlayer("User2") })
        val returnMessage = kickerBotSlackService.cancelMatch(uuid, teamId, userId)

        assertThat(returnMessage).isNull()
        verify(exactly = 1) { kickerMatchServiceSpy.cancelMatch(uuid, teamId, userId) }
        verify(exactly = 0) { slackMessageServiceMock.createMatchCanceledMessage() }
        verify(exactly = 0) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }

    @Test
    fun `cancelMatch timeout`() {
        val uuid = "UUID"
        val teamId = "TeamA"
        val userId = "User1"
        val returnMessage = kickerBotSlackService.cancelMatch(uuid, teamId, userId)

        assertThat(returnMessage).isNotNull
        verify(exactly = 1) { kickerMatchServiceSpy.cancelMatch(uuid, teamId, userId) }
        verify(exactly = 0) { slackMessageServiceMock.createMatchCanceledMessage() }
        verify(exactly = 1) { slackMessageServiceMock.createMatchTimeoutMessage() }
    }

    @Test
    fun receiveAccessToken() {
        val code = "code"
        kickerBotSlackService.receiveAccessToken(code)
        verify(exactly = 1) { slackMessageServiceMock.postAuthTokenVerifierAndSave(code) }
    }
}