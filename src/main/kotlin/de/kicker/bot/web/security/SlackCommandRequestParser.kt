package de.kicker.bot.web.security

import io.undertow.server.handlers.form.FormDataParser
import io.undertow.servlet.spec.HttpServletRequestImpl
import org.springframework.stereotype.Component
import java.lang.Exception
import java.net.URLEncoder
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

/**
 * Class for extracting relevant request parts for verifying slack requests.
 */
@Component
class SlackCommandRequestParser {

    /**
     * get the timestamp from given request when available or empty string
     */
    fun getTimestamp(request: HttpServletRequest): String {
        return request.getHeader("x-slack-request-timestamp") ?: ""
    }

    /**
     * get the signature from given request when available or empty string
     */
    fun getSignature(request: HttpServletRequest): String {
        return request.getHeader("x-slack-signature") ?: ""
    }

    /**
     * get the url encoded form body from given request when available or empty string
     */
    fun getFormBody(request: HttpServletRequest): String {
        return getFormDataKey(request).joinToString("&") { key -> "$key=${URLEncoder.encode(request.getParameter(key), "UTF-8")}" }
    }

    private fun getFormDataKey(request: HttpServletRequest): List<String> {
        try {
            when (request) {
                is HttpServletRequestWrapper -> {
                    when(val wrapper = request.request) {
                        is HttpServletRequestImpl -> return wrapper.exchange.getAttachment(FormDataParser.FORM_DATA).toList()
                    }
                }
            }
            return Collections.emptyList()
        } catch (e: Exception) {
            return Collections.emptyList()
        }
    }

}