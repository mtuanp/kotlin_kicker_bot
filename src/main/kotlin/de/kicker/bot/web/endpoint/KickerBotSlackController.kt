package de.kicker.bot.web.endpoint

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KickerBotSlackController {

    @PostMapping("/slack/kickergame")
    fun startKickerGame(@RequestParam(value = "name", defaultValue = "World") name: String) : String {
        return name
    }
}