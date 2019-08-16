package de.kicker.bot.endpoint

import me.ramswaroop.jbot.core.slack.SlackApiEndpoints
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SlackApiEndpoints {

    /**
     * Endpoint for Slack Api
     */
    @Value("\${slackApi}")
    lateinit var slackApi: String

    fun chat(): ChatApi {
        return ChatApi()
    }

    inner class ChatApi {
        fun postMessage() = "$slackApi/chat.postMessage"
    }

}