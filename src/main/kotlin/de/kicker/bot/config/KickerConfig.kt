package de.kicker.bot.config

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.kicker.bot.api.KickerMatch
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class KickerConfig {

    @Bean
    fun kickerMatchCache(): Cache<String, KickerMatch> {
        return CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

}