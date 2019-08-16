package de.kicker.bot.slack.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Option(text: String, value: String, description: String) {
}