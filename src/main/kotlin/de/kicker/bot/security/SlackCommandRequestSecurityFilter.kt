package de.kicker.bot.security

import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SlackCommandRequestSecurityFilter constructor(val parser: SlackCommandRequestParser, val verifier: SlackCommandRequestVerifier) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        logger.info("Check SlackRequest")
        val timestamp = parser.getTimestamp(request)
        val slackSignature = parser.getSignature(request)
        val formBody = parser.getFormBody(request)
        val verifierResult = verifier.verifySlackSignature(slackSignature, formBody, timestamp)
        if (!verifierResult) {
            logger.info("SlackRequest is not valid")
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
        } else {
            logger.info("SlackRequest is valid")
            filterChain.doFilter(request, response)
        }
    }

    @Throws(ServletException::class)
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (request.servletPath == "/slack/auth/redirect") {
            return true
        }
        return false
    }
}