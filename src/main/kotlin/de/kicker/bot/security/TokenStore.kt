package de.kicker.bot.security

interface TokenStore {

    /**
     * Get the token of given team_id
     */
    fun getToken(teamId: String): String

    /**
     * Save the token into the store.
     */
    fun saveToken(teamId: String, token: String): Unit
}