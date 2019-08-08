package de.kicker.bot.web

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@SpringBootApplication
class SlackKickerApplication

fun main(args: Array<String>) {
	runApplication<SlackKickerApplication>(*args)
}
