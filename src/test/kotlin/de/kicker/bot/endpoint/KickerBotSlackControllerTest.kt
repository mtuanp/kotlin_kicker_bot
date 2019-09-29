package de.kicker.bot.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import de.kicker.bot.api.MatchInteraction
import de.kicker.bot.service.KickerBotSlackService
import de.kicker.bot.slack.model.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.ramswaroop.jbot.core.slack.models.Channel
import me.ramswaroop.jbot.core.slack.models.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

internal class KickerBotSlackControllerTest {

    private lateinit var kickerBotSlackController: KickerBotSlackController
    private lateinit var kickerBotSlackServiceMock: KickerBotSlackService
    private lateinit var objectMapperMock: ObjectMapper

    @BeforeEach
    fun setup() {
        kickerBotSlackServiceMock = mockk(relaxed = true)
        objectMapperMock = mockk(relaxed = true)
        kickerBotSlackController = KickerBotSlackController().apply {
            kickerBotSlackService = kickerBotSlackServiceMock
            objectMapper = objectMapperMock
            async = false
        }
    }

    @Test
    fun startKickerGame() {
        val teamId = "TeamA"
        val user1 = "User1"
        val givenText = "BlaBla"
        val returnUrl = "returnUrl"
        val slackCommandRequest = SlackCommandRequest(team_id = teamId, user_id = user1, text = givenText, response_url = returnUrl)
        val returnValue = kickerBotSlackController.startKickerGame(slackCommandRequest)

        assertThat(returnValue).isEmpty()
        verify(exactly = 1) { kickerBotSlackServiceMock.startKickerGame(teamId, user1, givenText, returnUrl) }
    }

    @Test
    fun `callbackKickerGame join Game`() {
        val payload = "Payload"
        val givenCallbackId = "UUIDGame"
        val givenTeamId = "TeamA"
        val givenUser = "User1"
        val givenAttachment = ActionableAttachment()
        val givenChannelId = "ChannelX"
        val givenMessageTs = "1234545678789"
        every { objectMapperMock.readValue(payload, InteractiveMessage::class.java) } returns InteractiveMessage().apply {
            callbackId = givenCallbackId
            team = Team().apply { id = givenTeamId }
            user = User().apply { id = givenUser }
            originalMessage = OriginMessage().apply {
                attachments = arrayOf(ActionableAttachment(), ActionableAttachment(), givenAttachment)
            }
            actions = arrayOf(Action().apply { value = MatchInteraction.JOIN_MATCH.toString() })
            channel = Channel().apply { id = givenChannelId }
            messageTs = givenMessageTs
        }
        val returnValue = kickerBotSlackController.callbackKickerGame(payload)

        assertThat(returnValue).isNotNull
        verify(exactly = 1) { kickerBotSlackServiceMock.joinMatch(givenCallbackId, givenTeamId, givenUser, givenChannelId, givenMessageTs, givenAttachment) }
        verify(exactly = 0) { kickerBotSlackServiceMock.leaveMatch(givenCallbackId, givenTeamId, givenUser, givenAttachment) }
        verify(exactly = 0) { kickerBotSlackServiceMock.cancelMatch(givenCallbackId, givenTeamId, givenUser) }
    }

    @Test
    fun `callbackKickerGame leave Game`() {
        val payload = "Payload"
        val givenCallbackId = "UUIDGame"
        val givenTeamId = "TeamA"
        val givenUser = "User1"
        val givenAttachment = ActionableAttachment()
        val givenChannelId = "ChannelX"
        val givenMessageTs = "1234545678789"
        every { objectMapperMock.readValue(payload, InteractiveMessage::class.java) } returns InteractiveMessage().apply {
            callbackId = givenCallbackId
            team = Team().apply { id = givenTeamId }
            user = User().apply { id = givenUser }
            originalMessage = OriginMessage().apply {
                attachments = arrayOf(ActionableAttachment(), ActionableAttachment(), givenAttachment)
            }
            actions = arrayOf(Action().apply { value = MatchInteraction.LEAVE_MATCH.toString() })
            channel = Channel().apply { id = givenChannelId }
            messageTs = givenMessageTs
        }
        val returnValue = kickerBotSlackController.callbackKickerGame(payload)

        assertThat(returnValue).isNotNull
        verify(exactly = 0) { kickerBotSlackServiceMock.joinMatch(givenCallbackId, givenTeamId, givenUser, givenChannelId, givenMessageTs, givenAttachment) }
        verify(exactly = 1) { kickerBotSlackServiceMock.leaveMatch(givenCallbackId, givenTeamId, givenUser, givenAttachment) }
        verify(exactly = 0) { kickerBotSlackServiceMock.cancelMatch(givenCallbackId, givenTeamId, givenUser) }
    }

    @Test
    fun `callbackKickerGame cancel Game`() {
        val payload = "Payload"
        val givenCallbackId = "UUIDGame"
        val givenTeamId = "TeamA"
        val givenUser = "User1"
        val givenAttachment = ActionableAttachment()
        val givenChannelId = "ChannelX"
        val givenMessageTs = "1234545678789"
        every { objectMapperMock.readValue(payload, InteractiveMessage::class.java) } returns InteractiveMessage().apply {
            callbackId = givenCallbackId
            team = Team().apply { id = givenTeamId }
            user = User().apply { id = givenUser }
            originalMessage = OriginMessage().apply {
                attachments = arrayOf(ActionableAttachment(), ActionableAttachment(), givenAttachment)
            }
            actions = arrayOf(Action().apply { value = MatchInteraction.CANCEL_MATCH.toString() })
            channel = Channel().apply { id = givenChannelId }
            messageTs = givenMessageTs
        }
        val returnValue = kickerBotSlackController.callbackKickerGame(payload)

        assertThat(returnValue).isNotNull
        verify(exactly = 0) { kickerBotSlackServiceMock.joinMatch(givenCallbackId, givenTeamId, givenUser, givenChannelId, givenMessageTs, givenAttachment) }
        verify(exactly = 0) { kickerBotSlackServiceMock.leaveMatch(givenCallbackId, givenTeamId, givenUser, givenAttachment) }
        verify(exactly = 1) { kickerBotSlackServiceMock.cancelMatch(givenCallbackId, givenTeamId, givenUser) }
    }

    @Test
    fun oauthRedirect() {
        val givenCode = "MyCode"
        every { kickerBotSlackServiceMock.receiveAccessToken(givenCode) }.returns(true)
        val returnValue = kickerBotSlackController.oauthRedirect(givenCode, "state")

        assertThat(returnValue).isNotNull.matches { it.statusCode == HttpStatus.OK }
    }

    @Test
    fun `oauthRedirect is null`() {
        val givenCode = null
        val returnValue = kickerBotSlackController.oauthRedirect(givenCode, "state")

        assertThat(returnValue).isNotNull.matches { it.statusCode == HttpStatus.BAD_REQUEST }
    }

    @Test
    fun `oauthRedirect not correct token`() {
        val givenCode = "MyCode"
        every { kickerBotSlackServiceMock.receiveAccessToken(givenCode) }.returns(false)
        val returnValue = kickerBotSlackController.oauthRedirect(givenCode, "state")

        assertThat(returnValue).isNotNull.matches { it.statusCode == HttpStatus.BAD_REQUEST }
    }
}