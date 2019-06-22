package de.kicker.bot.web.config

import de.kicker.bot.web.security.SlackRequestSecurityFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import io.undertow.server.handlers.RequestDumpingHandler
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
import org.springframework.context.annotation.Bean


@Configuration
class KickerBotSecurityConfig : WebSecurityConfigurerAdapter(true) {

    @Autowired
    lateinit var slackFilter : SlackRequestSecurityFilter

    override fun configure(http: HttpSecurity?) {
        http?.addFilterAfter(slackFilter, BasicAuthenticationFilter::class.java);
    }


}