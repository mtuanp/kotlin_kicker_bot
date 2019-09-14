package de.kicker.bot.slack.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ConfirmDialog {

    var title: String = ""
    var text: String = ""
    @JsonProperty("ok_text")
    var okText: String = ""
    @JsonProperty("dismiss_text")
    var dismissText: String = ""
}