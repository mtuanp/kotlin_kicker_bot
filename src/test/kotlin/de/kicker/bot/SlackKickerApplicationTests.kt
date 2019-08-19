package de.kicker.bot

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.kicker.bot.api.KickerMatch
import de.kicker.bot.endpoint.KickerBotSlackController
import de.kicker.bot.security.SlackCommandRequestParser
import de.kicker.bot.security.SlackCommandRequestVerifier
import de.kicker.bot.service.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.web.client.RestTemplate

@WebMvcTest(controllers = [KickerBotSlackController::class])
class SlackKickerApplicationTests {

    companion object {
        val signature = "v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503"
        val timestamp = "1531420618"
        val content = "token=xyzz0WbapA4vBCDEFasx0q6G&team_id=T1DC2JH3J&team_domain=testteamnow&channel_id=G8PSS9T3V&channel_name=foobar&user_id=U2CERLKJA&user_name=roadrunner&command=%2Fwebhook-collect&text=&response_url=https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlgcZRskXaIFfN&trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447fce8b6703c"
    }

    @TestConfiguration
    class ControllerTestConfig {
        @Bean
        fun parser(): SlackCommandRequestParser {
            val mockSlackCommandRequestParser = mockk<SlackCommandRequestParser>()
            every { mockSlackCommandRequestParser.getSignature(any()) } returns signature
            every { mockSlackCommandRequestParser.getTimestamp(any()) } returns timestamp
            every { mockSlackCommandRequestParser.getFormBody(any()) } returns content
            return mockSlackCommandRequestParser
        }

        @Bean
        fun verifier() = spyk(SlackCommandRequestVerifier("8f742231b10e8888abcd99yyyzzz85a5"))

        @Bean
        fun restTemplate(): RestTemplate {
            return mockk(relaxed = true)
        }

        @Bean
        fun kickerMathService(): KickerMatchService {
            return mockk(relaxed = true)
        }

        @Bean
        fun cache(): Cache<String, KickerMatch> {
            return CacheBuilder.newBuilder().maximumSize(10).build()
        }

        @Bean
        fun endpoints(): SlackApiEndpoints {
            return mockk(relaxed = true)
        }

        @Bean
        fun slackMessageService(): SlackMessageService {
            return mockk(relaxed = true)
        }

        @Bean
        fun slackTokenService(): SlackTokenService {
            return mockk(relaxed = true)
        }

        @Bean
        fun slackTokenFileStorage(): SlackTokenFileStorage {
            return mockk(relaxed = true)
        }

        @Bean
        fun encryptionService(): EncryptionService {
            return mockk(relaxed = true)
        }
    }

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var parserMock: SlackCommandRequestParser

    @Autowired
    private lateinit var verifier: SlackCommandRequestVerifier

    @Test
    fun testingCorrectSlackCommandRequestShouldWork() {
        val kickerGameSlackRequest = MockMvcRequestBuilders.post("/kickergame")
                .header("x-slack-request-timestamp", timestamp)
                .header("x-slack-signature", signature)
                .contentType("application/x-www-form-urlencoded")
                .content(content)
        mvc.perform(kickerGameSlackRequest).andReturn()

        verify(exactly = 1) {
            parserMock.getSignature(any())
            parserMock.getTimestamp(any())
            parserMock.getFormBody(any())
            verifier.verifySlackSignature(signature, content, timestamp)
        }
    }


}
