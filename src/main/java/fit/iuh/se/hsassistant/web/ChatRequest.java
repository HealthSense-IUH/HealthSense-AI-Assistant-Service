package fit.iuh.se.hsassistant.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Một lượt hỏi. conversationId nhóm các lượt thành một hội thoại để Spring AI nạp lại ngữ cảnh.
 */
public record ChatRequest(
		@NotBlank @Size(max = 64) String conversationId,
		@NotBlank @Size(max = 4000) String message) {
}
