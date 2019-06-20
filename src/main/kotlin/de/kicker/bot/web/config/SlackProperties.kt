package de.kicker.bot.web.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "slack")
class SlackProperties {

    lateinit var signatureKey : String

}