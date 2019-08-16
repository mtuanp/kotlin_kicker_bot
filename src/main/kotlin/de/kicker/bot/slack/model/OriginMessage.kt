package de.kicker.bot.slack.model

import me.ramswaroop.jbot.core.slack.models.Message

class OriginMessage : Message() {
    var attachments: Array<ActionableAttachment> = arrayOf()
}