package de.kicker.bot.config

import de.kicker.bot.security.TokenStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class SlackApiConfig {

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

}