package de.kicker.bot.web.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import me.ramswaroop.jbot.core.slack.models.Attachment
import me.ramswaroop.jbot.core.slack.models.Channel
import me.ramswaroop.jbot.core.slack.models.Message
import me.ramswaroop.jbot.core.slack.models.User
import java.util.*

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
class InteractiveMessage {
    var type: String = ""
    var actions: Array<Action> = arrayOf()
    @JsonProperty("callback_id")
    var callbackId: String = ""
    var team: Optional<Team> = Optional.empty()
    var channel: Optional<Channel> = Optional.empty()
    var user: Optional<User> = Optional.empty()
    @JsonProperty("action_ts")
    var actionTs: String = ""
    @JsonProperty("message_ts")
    var messageTs: String = ""
    @JsonProperty("attachment_id")
    var attachmentId: Int = 0
    var token: String = ""
    var original_message: Optional<OriginMessage> = Optional.empty()
    var response_url: String = ""
    @JsonProperty("trigger_id")
    var triggerId: String = ""
    @JsonProperty("is_app_unfurl")
    var isAppUnfurl: Boolean = false
}