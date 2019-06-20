package de.kicker.bot.web

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SlackKickerApplication

fun main(args: Array<String>) {
	runApplication<SlackKickerApplication>(*args)
}
