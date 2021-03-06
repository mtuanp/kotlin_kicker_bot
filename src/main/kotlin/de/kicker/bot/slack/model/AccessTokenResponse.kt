package de.kicker.bot.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
class AccessTokenResponse : SlackResponse() {

    @JsonProperty("access_token")
    var accessToken: String = ""

    @JsonProperty("team_id")
    var teamId: String = ""

}