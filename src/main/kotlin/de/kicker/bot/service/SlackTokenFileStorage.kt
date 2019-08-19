package de.kicker.bot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.kicker.bot.extension.withReadLock
import de.kicker.bot.extension.withWriteLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.StampedLock
import javax.annotation.PostConstruct

@Service
class SlackTokenFileStorage(val cacheTokenMap: MutableMap<String, String> = ConcurrentHashMap()) {

    val logger: Logger = LoggerFactory.getLogger(SlackTokenFileStorage::class.java)

    @Value("\${slackTokenFile}")
    lateinit var slackTokenFilePath: String

    @Autowired
    lateinit var objectMapper: ObjectMapper

    val lock = StampedLock()

    @PostConstruct
    fun init() {
        lock.withWriteLock {
            val tokenFile = File(slackTokenFilePath)
            if (tokenFile.exists()) {
                try {
                    val importedMap: Map<String, String> = objectMapper.readValue(tokenFile.readText())
                    cacheTokenMap.putAll(importedMap)
                } catch (e: Exception) {
                    logger.error("Deserialization failed", e)
                }
            }
        }
    }

    fun retrieveToken(teamId: String): String? = lock.withReadLock {
        return@withReadLock cacheTokenMap[teamId]
    }

    fun saveTokenToFile(teamId: String, token: String) = lock.withWriteLock {
        cacheTokenMap[teamId] = token
        try {
            val tokenFile = File(slackTokenFilePath)
            if (!tokenFile.parentFile.exists()) {
                tokenFile.parentFile.mkdirs()
            }
            tokenFile.outputStream().use {
                objectMapper.writeValue(it, cacheTokenMap)
            }
        } catch (e: Exception) {
            logger.error("Serialization failed", e)
        }
    }

}