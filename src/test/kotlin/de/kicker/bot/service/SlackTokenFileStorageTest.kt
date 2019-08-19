package de.kicker.bot.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path


internal class SlackTokenFileStorageTest {


    @Test
    fun testingCallInitWithNotExistingFile(@TempDir tempDir: Path) {
        val cacheTokenMap: MutableMap<String, String> = mutableMapOf()
        val tokenStorage = SlackTokenFileStorage(cacheTokenMap).apply {
            slackTokenFilePath = tempDir.resolve("TestFile1").toString()
            objectMapper = ObjectMapper()
        }
        tokenStorage.init()
        assertTrue(cacheTokenMap.isEmpty())
    }

    @Test
    fun testingCallInitWithExistingFile() {
        val classLoader = SlackTokenFileStorageTest::class.java.classLoader
        val file = classLoader.getResource("TokenMapWith3Elements.txt")!!.file
        val cacheTokenMap: MutableMap<String, String> = mutableMapOf()
        val tokenStorage = SlackTokenFileStorage(cacheTokenMap).apply {
            slackTokenFilePath = file
            objectMapper = ObjectMapper()
        }
        tokenStorage.init()
        assertTrue(cacheTokenMap.size == 3)
        assertTrue(cacheTokenMap["TeamA"] == "TokenA")
        assertTrue(cacheTokenMap["TeamB"] == "TokenB")
        assertTrue(cacheTokenMap["TeamC"] == "TokenC")
    }

    @Test
    fun testingCallRetrieveTokenWithExistingToken() {
        val cacheTokenMap: MutableMap<String, String> = mutableMapOf(Pair("TeamA", "TokenA"), Pair("TeamB", "TokenB"))
        val tokenStorage = SlackTokenFileStorage(cacheTokenMap).apply {
            slackTokenFilePath = ""
            objectMapper = ObjectMapper()
        }
        val token = tokenStorage.retrieveToken("TeamB")
        assertNotNull(token)
        assertTrue(token == "TokenB")
    }

    @Test
    fun testingCallRetrieveTokenWithNotExistingToken() {
        val cacheTokenMap: MutableMap<String, String> = mutableMapOf(Pair("TeamA", "TokenA"), Pair("TeamB", "TokenB"))
        val tokenStorage = SlackTokenFileStorage(cacheTokenMap).apply {
            slackTokenFilePath = ""
            objectMapper = ObjectMapper()
        }
        val token = tokenStorage.retrieveToken("TeamV")
        assertNull(token)
    }

    @Test
    fun testingCallRetrieveTokenWithNotExistingTokenBecauseEmptyStorage() {
        val cacheTokenMap: MutableMap<String, String> = mutableMapOf()
        val tokenStorage = SlackTokenFileStorage(cacheTokenMap).apply {
            slackTokenFilePath = ""
            objectMapper = ObjectMapper()
        }
        val token = tokenStorage.retrieveToken("TeamV")
        assertNull(token)
    }

    @Test
    fun testingCallSaveTokenToFileWithNotExistingFile(@TempDir tempDir: Path) {
        val cacheTokenMap: MutableMap<String, String> = mutableMapOf(Pair("TeamA", "TokenA"), Pair("TeamB", "TokenB"), Pair("TeamC", "TokenC"))
        val testFile = tempDir.resolve("under/inner/TestFile.txt")
        assumeTrue(cacheTokenMap.size == 3)
        assumeFalse(testFile.toFile().exists())
        val tokenStorage = SlackTokenFileStorage(cacheTokenMap).apply {
            slackTokenFilePath = testFile.toString()
            objectMapper = ObjectMapper()
        }
        tokenStorage.saveTokenToFile("TeamD", "TokenD")
        assertTrue(cacheTokenMap.size == 4)
        assertTrue(cacheTokenMap["TeamD"] == "TokenD")
        assertTrue(testFile.toFile().exists())
        assertTrue(testFile.toFile().readText() == "{\"TeamA\":\"TokenA\",\"TeamB\":\"TokenB\",\"TeamC\":\"TokenC\",\"TeamD\":\"TokenD\"}")
    }


}