package de.kicker.bot.web

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource


@SpringBootTest()
@TestPropertySource(value = ["classpath:test-slack-config.properties"])
class SlackKickerApplicationTests {

	@Test
	fun contextLoads() {
	}

}
