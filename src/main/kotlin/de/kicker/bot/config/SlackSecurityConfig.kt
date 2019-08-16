package de.kicker.bot.config

import de.kicker.bot.security.SlackCommandRequestSecurityFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter


@Configuration
class SlackSecurityConfig : WebSecurityConfigurerAdapter(true) {

    @Autowired
    lateinit var slackCommandFilter: SlackCommandRequestSecurityFilter

    override fun configure(http: HttpSecurity?) {
        http?.addFilterAfter(slackCommandFilter, BasicAuthenticationFilter::class.java)
    }
}