package fit.iuh.se.hsassistant.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình ChatClient dùng chung cho cả service.
 *
 * Model và API key lấy từ application.properties (spring.ai.anthropic.*). Bộ nhớ hội thoại (ChatMemory)
 * do Spring AI tự tạo và lưu vào bảng SPRING_AI_CHAT_MEMORY trong PostgreSQL.
 */
@Configuration
public class AssistantAiConfig {

	/**
	 * Lời dặn hệ thống: giữ mô hình ở vai trò diễn giải dữ liệu, không chẩn đoán, không kê thuốc.
	 * Khi thêm tool (đọc lịch sử đo, bữa ăn, lịch khám) thì bổ sung hướng dẫn dùng tool vào đây.
	 */
	static final String SYSTEM_PROMPT = """
			Bạn là trợ lý sức khoẻ của HealthSense, trả lời bằng tiếng Việt, ngắn gọn và dễ hiểu.
			Bạn chỉ diễn giải số liệu và thông tin có sẵn của người dùng; bạn không chẩn đoán bệnh,
			không kê đơn, không thay đổi liều thuốc. Khi người dùng mô tả đau ngực, ngất, khó thở nặng
			hoặc yếu nửa người, hãy dừng trò chuyện và khuyên gọi cấp cứu 115 ngay.
			Mọi kết luận về rung nhĩ phải được bác sĩ xác nhận bằng điện tâm đồ.
			""";

	@Bean
	ChatClient assistantChatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
		return builder
				.defaultSystem(SYSTEM_PROMPT)
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build();
	}

}
