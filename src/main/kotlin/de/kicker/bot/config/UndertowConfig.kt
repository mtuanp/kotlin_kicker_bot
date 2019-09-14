package de.kicker.bot.config

import io.undertow.server.handlers.RequestDumpingHandler
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class UndertowConfig {

    @Bean
    @Profile("logging")
    fun undertowServletWebServerFactory(): UndertowServletWebServerFactory {
        val factory = UndertowServletWebServerFactory()
        factory.addDeploymentInfoCustomizers(org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer { deploymentInfo -> deploymentInfo.addInitialHandlerChainWrapper { handler -> RequestDumpingHandler(handler) } })
        return factory
    }

}