package de.kicker.bot.util

/**
 * Util class for splitting a string with slack userIds.
 */
class SlackUserIdSplitter {

    companion object {
        private val regex = "<@(\\w*)\\|".toRegex()

        /**
         * Find all user id with a regex and return it as Collection of userIds
         */
        fun toIdList(args: String): Collection<String> {
            return regex.findAll(args).map { it.groupValues[1] }.distinct().toList()
        }

    }
}