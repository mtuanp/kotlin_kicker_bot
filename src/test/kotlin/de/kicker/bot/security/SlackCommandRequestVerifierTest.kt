package de.kicker.bot.security

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SlackCommandRequestVerifierTest {

    val verfifier = SlackCommandRequestVerifier("8f742231b10e8888abcd99yyyzzz85a5")
    val requestBody = "token=xyzz0WbapA4vBCDEFasx0q6G&team_id=T1DC2JH3J&team_domain=testteamnow&channel_id=G8PSS9T3V&channel_name=foobar&user_id=U2CERLKJA&user_name=roadrunner&command=%2Fwebhook-collect&text=&response_url=https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlgcZRskXaIFfN&trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447fce8b6703c"

    @Test
    fun whenGivenCorecctSignatureThenTrueShouldReturn() {
        val isCorrect = verfifier.verifySlackSignature("v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503", requestBody, "1531420618")
        assertTrue(isCorrect)
    }

    @Test
    fun whenGivenIncorrectTimestampThenFalseShouldReturn() {
        val isCorrect = verfifier.verifySlackSignature("v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503", requestBody, "123456789")
        assertFalse(isCorrect)
    }

    @Test
    fun whenGivenIncorrectRequestBodyThenFalseShouldReturn() {
        val isCorrect = verfifier.verifySlackSignature("v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503", "Foo", "1531420618")
        assertFalse(isCorrect)
    }

    @Test
    fun whenGivenIncorrectSignatureThenFalseShouldReturn() {
        val isCorrect = verfifier.verifySlackSignature("v0=458412356886435a452", requestBody, "1531420618")
        assertFalse(isCorrect)
    }
}