package de.kicker.bot.web.api

import me.ramswaroop.jbot.core.slack.models.Message

class OriginMessage : Message() {
    var attachments:Array<ActionableAttachment> = arrayOf()
}