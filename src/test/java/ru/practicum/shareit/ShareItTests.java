package ru.practicum.shareit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test that the Spring context loads for the application.
 */
@SpringBootTest
@DisplayName("Application context smoke test")
class ShareItTests {

	@Test
	@DisplayName("context loads")
	void contextLoads() {
	}
}