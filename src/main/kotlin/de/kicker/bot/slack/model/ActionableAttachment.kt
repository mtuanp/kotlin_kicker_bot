package de.kicker.bot.slack.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import me.ramswaroop.jbot.core.slack.models.Attachment

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ActionableAttachment : Attachment() {
    @JsonProperty("callback_id")
    var callbackId: String = ""
    var actions: Array<Action> = arrayOf()
}