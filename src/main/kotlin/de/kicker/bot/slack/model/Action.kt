package de.kicker.bot.slack.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Action {
    var name: String = ""
    var text: String = ""
    var type: String = ""
    var id: Int = 0
    var value: String = ""
    var confirm: String = ""
    var style: String = ""
    var options: Array<Option> = arrayOf()
    @JsonProperty("option_groups")
    var optionGroups: Array<Option> = arrayOf()
    @JsonProperty("data_source")
    var dataSource: String = ""
    @JsonProperty("selected_options")
    var selectedOptions: Array<Option> = arrayOf()
    @JsonProperty("min_query_length")
    var minQuery_length: Int = 0
}