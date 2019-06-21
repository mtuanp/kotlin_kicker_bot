package de.kicker.bot.web.security

import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SlackRequestSecurityFilter constructor(val verifier: SlackCommandRequestVerifier) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        logger.info("Check SlackRequest")
        val timestamp = request.getHeader("x-slack-request-timestamp") ?: ""
        val slackSignature = request.getHeader("x-slack-signature") ?: ""
        val requestBody = getBody(request)
        val verifierResult = verifier.verifySlackSignature(slackSignature, requestBody, timestamp)
        if (!verifierResult) {
            logger.info("SlackRequest is not valid")
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
        } else {
            logger.info("SlackRequest is valid")
        }
    }

    private fun getBody(req: HttpServletRequest): String = req.inputStream.bufferedReader().use { it.readText() }

}