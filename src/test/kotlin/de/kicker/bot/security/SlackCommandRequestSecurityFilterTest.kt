package de.kicker.bot.security

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class SlackCommandRequestSecurityFilterTest {

    private lateinit var slackCommandRequestSecurityFilter: SlackCommandRequestSecurityFilter
    private lateinit var mockParser: SlackCommandRequestParser
    private lateinit var mockVerifier: SlackCommandRequestVerifier

    @BeforeEach
    fun setup() {
        mockParser = mockk(relaxed = true)
        mockVerifier = mockk(relaxed = true)
        slackCommandRequestSecurityFilter = SlackCommandRequestSecurityFilter(mockParser, mockVerifier)
    }

    @Test
    fun doFilterInternal() {
        val mockRequest: HttpServletRequest = mockk(relaxed = true)
        val mockResponse: HttpServletResponse = mockk(relaxed = true)
        val mockFilterChain: FilterChain = mockk(relaxed = true)
        every { mockVerifier.verifySlackSignature("", "", "") }.returns(true)
        slackCommandRequestSecurityFilter.doFilterInternalk(mockRequest, mockResponse, mockFilterChain)

        verifyOrder {
            mockParser.getTimestamp(match { it == mockRequest })
            mockParser.getSignature(match { it == mockRequest })
            mockParser.getFormBody(match { it == mockRequest })
        }
        verify { mockFilterChain.doFilter(mockRequest, mockResponse) }
        verify(exactly = 0) { mockResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied") }
    }

    @Test
    fun doFilterInternalFalse() {
        val mockRequest: HttpServletRequest = mockk(relaxed = true)
        val mockResponse: HttpServletResponse = mockk(relaxed = true)
        val mockFilterChain: FilterChain = mockk(relaxed = true)
        every { mockVerifier.verifySlackSignature("", "", "") }.returns(false)
        slackCommandRequestSecurityFilter.doFilterInternalk(mockRequest, mockResponse, mockFilterChain)

        verifyOrder {
            mockParser.getTimestamp(match { it == mockRequest })
            mockParser.getSignature(match { it == mockRequest })
            mockParser.getFormBody(match { it == mockRequest })
        }
        verify(exactly = 1) { mockResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied") }
        verify(exactly = 0) { mockFilterChain.doFilter(mockRequest, mockResponse) }
    }

    @Test
    fun shouldNotFilter() {
        val mockRequest: HttpServletRequest = mockk(relaxed = true)
        every { mockRequest.requestURI }.returns("/slack/all")
        val returnValue = slackCommandRequestSecurityFilter.shouldNotFilterk(mockRequest)
        assertThat(returnValue).isFalse()
    }

    @Test
    fun shouldNotFilterTrue() {
        val mockRequest: HttpServletRequest = mockk(relaxed = true)
        every { mockRequest.requestURI }.returns("/slack/auth/redirect")
        val returnValue = slackCommandRequestSecurityFilter.shouldNotFilterk(mockRequest)
        assertThat(returnValue).isTrue()
    }
}