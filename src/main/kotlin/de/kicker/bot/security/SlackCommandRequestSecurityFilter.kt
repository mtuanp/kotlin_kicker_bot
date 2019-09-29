package de.kicker.bot.security

import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Filter for checking slack request.
 */
@Component
class SlackCommandRequestSecurityFilter constructor(val parser: SlackCommandRequestParser, val verifier: SlackCommandRequestVerifier) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        doFilterInternalk(request, response, filterChain)
    }

    internal fun doFilterInternalk(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        logger.debug("Check SlackRequest")
        val timestamp = parser.getTimestamp(request)
        val slackSignature = parser.getSignature(request)
        val formBody = parser.getFormBody(request)
        val verifierResult = verifier.verifySlackSignature(slackSignature, formBody, timestamp)
        if (!verifierResult) {
            logger.warn("SlackRequest is not valid")
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
        } else {
            logger.debug("SlackRequest is valid")
            filterChain.doFilter(request, response)
        }
    }

    @Throws(ServletException::class)
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return shouldNotFilterk(request)
    }

    internal fun shouldNotFilterk(request: HttpServletRequest): Boolean {
        if ((!request.requestURI.startsWith("/slack/")) || request.requestURI == "/slack/auth/redirect") {
            return true
        }
        return false
    }
}