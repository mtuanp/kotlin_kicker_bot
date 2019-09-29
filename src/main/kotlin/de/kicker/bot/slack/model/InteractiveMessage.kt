package de.kicker.bot.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import me.ramswaroop.jbot.core.slack.models.Channel
import me.ramswaroop.jbot.core.slack.models.User

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
class InteractiveMessage {
    var type: String = ""
    var actions: Array<Action> = arrayOf()
    @JsonProperty("callback_id")
    var callbackId: String = ""
    lateinit var team: Team
    lateinit var channel: Channel
    lateinit var user: User
    @JsonProperty("action_ts")
    var actionTs: String = ""
    @JsonProperty("message_ts")
    var messageTs: String = ""
    @JsonProperty("attachment_id")
    var attachmentId: Int = 0
    var token: String = ""
    @JsonProperty("original_message")
    lateinit var originalMessage: OriginMessage
    @JsonProperty("response_url")
    var responseUrl: String = ""
    @JsonProperty("trigger_id")
    var triggerId: String = ""
    @JsonProperty("is_app_unfurl")
    var isAppUnfurl: Boolean = false
}