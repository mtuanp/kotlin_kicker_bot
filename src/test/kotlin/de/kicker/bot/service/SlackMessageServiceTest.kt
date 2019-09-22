package de.kicker.bot.service

import de.kicker.bot.slack.model.AccessTokenResponse
import de.kicker.bot.slack.model.ActionableAttachment
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.ramswaroop.jbot.core.slack.models.Attachment
import me.ramswaroop.jbot.core.slack.models.RichMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

internal class SlackMessageServiceTest {

    private lateinit var slackMessageService: SlackMessageService
    lateinit var mockRestTemplate: RestTemplate
    lateinit var mockSlackTokenService: SlackTokenService
    lateinit var mockLogger: Logger

    @BeforeEach
    fun setup() {
        mockRestTemplate = mockk(relaxed = true)
        mockSlackTokenService = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        slackMessageService = SlackMessageService().apply {
            restTemplate = mockRestTemplate
            slackApiEndpoints = SlackApiEndpoints().apply { slackApi = "slack" }
            slackTokenService = mockSlackTokenService
            logger = mockLogger
        }
    }

    @Test
    fun postInitialMessageToChannelTest() {
        val slot = slot<RichMessage>()
        every { mockRestTemplate.postForEntity("Url", capture(slot), String::class.java) }.returns(ResponseEntity(HttpStatus.OK))
        slackMessageService.postInitialMessageToChannel("TestUID", "Player_1", "Url")
        verify(exactly = 1) { mockRestTemplate.postForEntity("Url", any<RichMessage>(), String::class.java) }
        assertThat(slot.captured).matches { it.responseType == "in_channel" }
        assertThat(slot.captured.attachments).hasSize(3)
                .matches { it[0].text.contains("Hello") }
                .matches { it[1].text.contains("Actual players") && it[1].text.contains("Player_1") }
                .matches { it[2].title.contains("join?") && (it[2] as ActionableAttachment).actions.size == 3 }
    }

    @Test
    fun postInitialMessageToChannelExceptionTest() {
        every { mockRestTemplate.postForEntity("Url", any<RichMessage>(), String::class.java) }.throws(RestClientException("Testing"))
        slackMessageService.postInitialMessageToChannel("TestUID", "Player_1", "Url")
        verify(exactly = 1) { mockLogger.error("couldn't send message to slack", any<RestClientException>()) }
    }

    @Test
    fun postInitialReadyMatchMessageToChannelTest() {
        val slot = slot<RichMessage>()
        every { mockRestTemplate.postForEntity("Url", capture(slot), String::class.java) }.returns(ResponseEntity(HttpStatus.OK))
        slackMessageService.postInitialReadyMatchMessageToChannel("Player_1", "Url")
        verify(exactly = 1) { mockRestTemplate.postForEntity("Url", any<RichMessage>(), String::class.java) }
        assertThat(slot.captured).matches { it.responseType == "in_channel" }
        assertThat(slot.captured.attachments).hasSize(3)
                .matches { it[0].text.contains("Hello") }
                .matches { it[1].text.contains("Actual players") && it[1].text.contains("Player_1") }
                .matches { it[2].text.contains("ready") && it[2] is Attachment }
    }

    @Test
    fun postInitialReadyMatchMessageToChannelExceptionTest() {
        every { mockRestTemplate.postForEntity("Url", any<RichMessage>(), String::class.java) }.throws(RestClientException("Testing"))
        slackMessageService.postInitialReadyMatchMessageToChannel("Player_1", "Url")
        verify(exactly = 1) { mockLogger.error("couldn't send message to slack", any<RestClientException>()) }
    }


    @Test
    fun createAddPlayerMessageTest() {
        val originAttach = Attachment()
        val message = slackMessageService.createActiveMatchMessage(originAttach, "Player_1", false)
        assertThat(message).matches { it.responseType == "in_channel" }
        assertThat(message.attachments).hasSize(3)
                .matches { it[0].text.contains("Hello") }
                .matches { it[1].text.contains("Actual players") && it[1].text.contains("Player_1") }
                .matches { it[2] === originAttach }
    }

    @Test
    fun createAddPlayerMessageReadyMatchTest() {
        val originAttach = Attachment()
        val message = slackMessageService.createActiveMatchMessage(originAttach, "Player_1", true)
        assertThat(message).matches { it.responseType == "in_channel" }
        assertThat(message.attachments).hasSize(3)
                .matches { it[0].text.contains("Hello") }
                .matches { it[1].text.contains("Actual players") && it[1].text.contains("Player_1") }
                .matches { it[2] !== originAttach }
                .matches { it[2].text.contains("ready") && it[2] is Attachment }
    }

    @Test
    fun createMatchTimeoutMessageTest() {
        val message = slackMessageService.createMatchTimeoutMessage()
        assertThat(message).matches { it.responseType == "in_channel" }
        assertThat(message.attachments).hasSize(1)
                .matches { it[0].text.contains("no longer available, please start") }
    }

    @Test
    fun createMatchCanceledMessageTest() {
        val message = slackMessageService.createMatchCanceledMessage()
        assertThat(message).matches { it.responseType == "in_channel" }
        assertThat(message.attachments).hasSize(1)
                .matches { it[0].text.contains("canceled by user") }
    }

    @Test
    fun postUserGoMessageNotificationTest() {
        val messageSlot = slot<HttpEntity<RichMessage>>()
        val urlSlot = slot<String>()
        val teamIdSlot = slot<String>()
        every { mockSlackTokenService.getToken(capture(teamIdSlot)) }.returns("Token")
        every { mockRestTemplate.postForEntity(capture(urlSlot), capture(messageSlot), String::class.java) }.returns(ResponseEntity(HttpStatus.OK))
        slackMessageService.postUserGoMessageNotification("TeamR", "Player1", "All Players")

        assertThat(teamIdSlot.captured).isEqualTo("TeamR")
        assertThat(urlSlot.captured).isEqualTo("slack/chat.postMessage")
        assertThat(messageSlot.captured.headers.contentType).isEqualTo(MediaType.APPLICATION_JSON)
        assertThat(messageSlot.captured.headers["Authorization"]!!.first()).isEqualTo("Bearer Token")
        assertThat(messageSlot.captured.body!!.channel).isEqualTo("Player1")
        assertThat(messageSlot.captured.body!!.attachments).hasSize(1)
                .matches { it[0].text.contains("Go go go!") }
                .matches { it[0].text.contains("All Players") }
    }

    @Test
    fun postUserGoMessageNotificationExceptionTest() {
        every { mockRestTemplate.postForEntity(any<String>(), any<HttpEntity<RichMessage>>(), String::class.java) }.throws(RestClientException("Testing"))
        slackMessageService.postUserGoMessageNotification("TeamR", "Player1", "All Players")
        verify(exactly = 1) { mockLogger.error("couldn't send message to slack", any<RestClientException>()) }
    }

    @Test
    fun postAuthTokenVerifierAndSaveTest() {
        val messageSlot = slot<HttpEntity<MultiValueMap<String, String>>>()
        val urlSlot = slot<String>()
        val resultBody = AccessTokenResponse().apply {
            ok = true
            accessToken = "Token"
            teamId = "TeamR"
        }
        every { mockRestTemplate.postForEntity(capture(urlSlot), capture(messageSlot), AccessTokenResponse::class.java) }.returns(ResponseEntity(resultBody, HttpStatus.OK))
        val result = slackMessageService.postAuthTokenVerifierAndSave("CodeX")

        verify(exactly = 1) { mockSlackTokenService.saveToken(any(), any()) }
        assertThat(result).isTrue()
        assertThat(urlSlot.captured).isEqualTo("slack/oauth.access")
        assertThat(messageSlot.captured.headers.contentType).isEqualTo(MediaType.APPLICATION_FORM_URLENCODED)
        assertThat(messageSlot.captured.body).hasSize(4)
                .matches {it.containsKey("client_id")}
                .matches {it.containsKey("client_secret")}
                .matches {it.containsKey("code")}
                .matches {it.containsKey("single_channel")}
    }

    @Test
    fun postAuthTokenVerifierAndSaveFalseTest() {
        val messageSlot = slot<HttpEntity<MultiValueMap<String, String>>>()
        val urlSlot = slot<String>()
        val resultBody = AccessTokenResponse().apply {
            ok = false
            accessToken = "Token"
            teamId = "TeamR"
        }
        every { mockRestTemplate.postForEntity(capture(urlSlot), capture(messageSlot), AccessTokenResponse::class.java) }.returns(ResponseEntity(resultBody, HttpStatus.OK))
        val result = slackMessageService.postAuthTokenVerifierAndSave("CodeX")

        verify(exactly = 0) { mockLogger.error("couldn't send message to slack", any<RestClientException>()) }
        verify(exactly = 0) { mockSlackTokenService.saveToken(any(), any()) }
        assertThat(result).isFalse()
    }

    @Test
    fun postAuthTokenVerifierAndSaveFalse2Test() {
        val messageSlot = slot<HttpEntity<MultiValueMap<String, String>>>()
        val urlSlot = slot<String>()
        val resultBody = AccessTokenResponse().apply {
            ok = true
            accessToken = "Token"
            teamId = "TeamR"
        }
        every { mockRestTemplate.postForEntity(capture(urlSlot), capture(messageSlot), AccessTokenResponse::class.java) }.returns(ResponseEntity(resultBody, HttpStatus.BAD_REQUEST))
        val result = slackMessageService.postAuthTokenVerifierAndSave("CodeX")

        verify(exactly = 0) { mockLogger.error("couldn't send message to slack", any<RestClientException>()) }
        verify(exactly = 0) { mockSlackTokenService.saveToken(any(), any()) }
        assertThat(result).isFalse()
    }

    @Test
    fun postAuthTokenVerifierAndSaveExceptionTest() {
        every { mockRestTemplate.postForEntity(any<String>(), any<HttpEntity<MultiValueMap<String, String>>>(), AccessTokenResponse::class.java) }.throws(RestClientException("Testing"))
        val result = slackMessageService.postAuthTokenVerifierAndSave("CodeX")
        verify(exactly = 1) { mockLogger.error("couldn't send message to slack", any<RestClientException>()) }
        verify(exactly = 0) { mockSlackTokenService.saveToken(any(), any()) }
        assertThat(result).isFalse()
    }
}