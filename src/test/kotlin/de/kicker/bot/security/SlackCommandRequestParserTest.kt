package de.kicker.bot.security

import io.mockk.every
import io.mockk.mockk
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.form.FormData
import io.undertow.server.handlers.form.FormDataParser
import io.undertow.servlet.spec.HttpServletRequestImpl
import io.undertow.servlet.spec.ServletContextImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

internal class SlackCommandRequestParserTest {

    val parser = SlackCommandRequestParser()

    @Test
    fun getTimestamp() {
        val timestampValue = "TimeS"
        val mockRequest: HttpServletRequest = mockk(relaxed = true)
        every { mockRequest.getHeader("x-slack-request-timestamp") }.returns(timestampValue)
        val timestamp = parser.getTimestamp(mockRequest)
        assertThat(timestamp).isNotEmpty().isEqualTo(timestampValue)
    }

    @Test
    fun getSignature() {
        val signatureValue = "SignatureS"
        val mockRequest: HttpServletRequest = mockk(relaxed = true)
        every { mockRequest.getHeader("x-slack-signature") }.returns(signatureValue)
        val signature = parser.getSignature(mockRequest)
        assertThat(signature).isNotEmpty().isEqualTo(signatureValue)
    }

    @Test
    fun getFormBody() {
        val mockExchange: HttpServerExchange = mockk(relaxed = true)
        val mockServlet: ServletContextImpl = mockk(relaxed = true)
        val requestWrapper: HttpServletRequestWrapper = mockk(relaxed = true)
        val request = HttpServletRequestImpl(mockExchange, mockServlet)
        every { requestWrapper.request }.returns(request)
        val formData = FormData(1)
        formData.add("FormA", "Value1")
        every { mockExchange.getAttachment(FormDataParser.FORM_DATA) }.returns(formData)


        val formBody = parser.getFormBody(requestWrapper)
        assertThat(formBody).isNotEmpty().isEqualTo("FormA=")
    }
}