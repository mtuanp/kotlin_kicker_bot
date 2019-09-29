package de.kicker.bot.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import me.ramswaroop.jbot.core.slack.models.RichMessage

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
class RichMessageTimestamp : RichMessage() {

    var ts: String = ""
}