package fit.iuh.se.hsassistant.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * API tối thiểu để kiểm tra đường đi tới Anthropic API. Các tính năng (nhật ký yếu tố kích hoạt,
 * báo cáo tuần, tóm tắt tiền khám) sẽ là controller riêng, dùng lại cùng ChatClient.
 *
 * TODO bảo mật: thêm spring-boot-starter-oauth2-resource-server và xác thực JWT của Core Service
 * trước khi mở ra ngoài; hiện conversationId do client tự đặt chỉ phù hợp cho giai đoạn phát triển.
 */
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

	private final ChatClient assistantChatClient;

	@Value("${spring.ai.anthropic.chat.model:unknown}")
	private String modelId;

	@GetMapping("/info")
	public Map<String, String> info() {
		return Map.of(
				"service", "HealthSense AI Assistant Service",
				"provider", "Anthropic Claude API",
				"model", modelId);
	}

	@PostMapping("/chat")
	public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
		var response = assistantChatClient.prompt()
				.user(request.message())
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
				.call()
				.chatResponse();
		// Claude 5 có thể trả nhiều khối nội dung, khối đầu đôi khi trống; ghép mọi khối có chữ thay vì chỉ lấy khối đầu
		String answer = response == null ? "" : response.getResults().stream()
				.map(g -> g.getOutput().getText())
				.filter(t -> t != null && !t.isBlank())
				.collect(Collectors.joining("\n"))
				.trim();
		return new ChatResponse(request.conversationId(), answer);
	}

}
