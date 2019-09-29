package de.kicker.bot.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
open class SlackResponse {
    var ok: Boolean = false
    var channel: String = ""
    var ts: String = ""
    var error: String = ""


    override fun toString(): String {
        return "SlackResponse(ok=$ok, channel='$channel', ts='$ts', error='$error')"
    }
}