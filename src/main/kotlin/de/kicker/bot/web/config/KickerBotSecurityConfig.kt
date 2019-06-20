package de.kicker.bot.web.config

import de.kicker.bot.web.security.SlackRequestSecurityFilter
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
class KickerBotSecurityConfig : WebSecurityConfigurerAdapter(true) {

    override fun configure(http: HttpSecurity?) {
        http?.addFilterAfter(SlackRequestSecurityFilter(), BasicAuthenticationFilter::class.java);
    }

}