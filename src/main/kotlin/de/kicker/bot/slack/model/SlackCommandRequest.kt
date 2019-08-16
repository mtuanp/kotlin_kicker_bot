package de.kicker.bot.slack.model

data class SlackCommandRequest(val token: String = "", val command: String = "", val text: String = "",
                               val response_url: String = "", val trigger_id: String = "", val user_id: String = "",
                               val user_name: String = "", val team_id: String = "", val enterprise_id: String = "",
                               val channel_id: String = "") {

}