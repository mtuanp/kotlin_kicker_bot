package de.kicker.bot.web.config

import io.undertow.server.handlers.RequestDumpingHandler
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UndertowConfig {

    @Bean
    fun undertowServletWebServerFactory(): UndertowServletWebServerFactory {
        val factory = UndertowServletWebServerFactory()
        factory.addDeploymentInfoCustomizers(org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer { deploymentInfo -> deploymentInfo.addInitialHandlerChainWrapper { handler -> RequestDumpingHandler(handler) } })
        return factory
    }

}