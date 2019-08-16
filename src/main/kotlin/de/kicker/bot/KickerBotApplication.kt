package de.kicker.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SlackKickerApplication

fun main(args: Array<String>) {
    runApplication<SlackKickerApplication>(*args)
}
