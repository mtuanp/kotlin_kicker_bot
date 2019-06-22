package de.kicker.bot.web.security

import io.undertow.server.handlers.form.FormDataParser
import io.undertow.servlet.spec.HttpServletRequestImpl
import org.springframework.security.web.firewall.FirewalledRequest
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.lang.Exception
import java.net.URLEncoder
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SlackRequestSecurityFilter constructor(val verifier: SlackCommandRequestVerifier) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        logger.info("Check SlackRequest")
        val timestamp = request.getHeader("x-slack-request-timestamp") ?: ""
        val slackSignature = request.getHeader("x-slack-signature") ?: ""
        val formKeyList = getFormDataKey(request)
        val formBody = formKeyList.joinToString("&") { key -> "$key=${URLEncoder.encode(request.getParameter(key), "UTF-8")}" }
        val verifierResult = verifier.verifySlackSignature(slackSignature, formBody, timestamp)
        if (!verifierResult) {
            logger.info("SlackRequest is not valid")
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
        } else {
            logger.info("SlackRequest is valid")
        }
    }

    private fun getFormDataKey(request: HttpServletRequest) : List<String> = try {
        ((request as FirewalledRequest).request as HttpServletRequestImpl).exchange.getAttachment(FormDataParser.FORM_DATA).toList()
    } catch (e: Exception) {
        Collections.emptyList()
    }

}