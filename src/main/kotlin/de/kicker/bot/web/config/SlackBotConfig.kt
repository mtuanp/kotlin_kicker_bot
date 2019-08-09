package de.kicker.bot.web.config

import me.ramswaroop.jbot.core.slack.Bot
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class SlackBotConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

}