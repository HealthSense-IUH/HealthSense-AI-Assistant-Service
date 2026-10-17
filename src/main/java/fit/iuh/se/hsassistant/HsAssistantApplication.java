package fit.iuh.se.hsassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * HealthSense AI Assistant Service.
 *
 * Trợ lý AI đứng cạnh Core Service: nhận câu nói hoặc chữ của người dùng, gọi Claude qua Anthropic API,
 * lấy dữ liệu thật qua API của Core Service (tool calling) và trả về lời diễn giải. Mọi quyết định y khoa
 * vẫn thuộc về bác sĩ; service này không chẩn đoán.
 */
@SpringBootApplication
@EnableFeignClients
public class HsAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(HsAssistantApplication.class, args);
	}

}
