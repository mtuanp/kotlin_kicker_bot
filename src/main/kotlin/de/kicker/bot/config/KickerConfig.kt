package de.kicker.bot.config

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.kicker.bot.api.KickerMatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.TimeUnit

@Configuration
class KickerConfig {
    val logger: Logger = LoggerFactory.getLogger(KickerConfig::class.java)

    @Bean
    @Profile("prod")
    fun kickerMatchCacheProd(): Cache<String, KickerMatch> {
        logger.info("load production cache settings")
        return CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Bean
    @Profile("dev")
    fun kickerMatchCacheDev(): Cache<String, KickerMatch> {
        logger.info("load dev cache settings")
        return CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
    }

}