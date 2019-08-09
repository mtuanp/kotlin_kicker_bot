package de.kicker.bot.web.api

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Option(text: String, value: String, description: String) {
}