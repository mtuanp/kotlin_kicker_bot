package de.kicker.bot.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SlackApiEndpoints {

    /**
     * Endpoint for Slack Api
     */
    @Value("\${slackApi:https://slack.com/api}")
    lateinit var slackApi: String

    fun chat(): ChatApi {
        return ChatApi()
    }

    fun auth(): AuthApi {
        return AuthApi()
    }

    inner class ChatApi {

        private val chat = "chat"

        fun postMessage() = "$slackApi/$chat.postMessage"

        fun deleteMessage() = "$slackApi/$chat.delete"

        fun postEphemeral() = "$slackApi/$chat.postEphemeral"
    }

    inner class AuthApi {
        fun access() = "$slackApi/oauth.access"
    }

}