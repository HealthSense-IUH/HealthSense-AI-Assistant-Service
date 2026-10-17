package fit.iuh.se.hsassistant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Context phải nạp được mà không cần Postgres, RabbitMQ hay API key thật.
 * Cấu hình test đặt ngay tại đây (H2 trong bộ nhớ) thay vì một file application-test riêng,
 * giữ resources/ chỉ có application.properties và application-dev.properties như Core Service.
 */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:assistant;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.properties.hibernate.default_schema=",
		"spring.rabbitmq.host=localhost",
		"spring.rabbitmq.port=5672",
		"spring.rabbitmq.username=guest",
		"spring.rabbitmq.password=guest",
		"spring.rabbitmq.virtual-host=/",
		"app.rabbitmq.exchange=health.record.exchange",
		"app.rabbitmq.queue.assistant=assistant.events.queue",
		"spring.ai.anthropic.api-key=test-key-not-used",
		"spring.ai.anthropic.chat.model=claude-sonnet-5-5",
		"spring.ai.chat.memory.repository.jdbc.initialize-schema=never",
		"app.core.base-url=http://localhost:8080",
		"management.health.rabbit.enabled=false"
})
class HsAssistantApplicationTests {

	@Test
	void contextLoads() {
	}

}
