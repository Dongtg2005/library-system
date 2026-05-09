package com.lms.library.application.service;

import com.lms.library.application.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient.Builder webClientBuilder;

    /**
     * Gọi Gemini API với system prompt, lịch sử hội thoại và tin nhắn mới nhất.
     *
     * @param systemPrompt  System instruction được inject dữ liệu DB thực tế
     * @param history       Lịch sử hội thoại từ Frontend (có thể rỗng)
     * @param userMessage   Tin nhắn mới nhất của người dùng
     */
    public String chat(String systemPrompt, List<ChatRequest.ChatMessage> history, String userMessage) {
        try {
            WebClient client = webClientBuilder.build();

            // ── Build contents: history + tin nhắn hiện tại ─────────────────
            List<Map<String, Object>> contents = new ArrayList<>();

            // Thêm lịch sử trước (tối đa 20 tin như đã validate ở ChatRequest)
            if (history != null) {
                for (ChatRequest.ChatMessage msg : history) {
                    contents.add(Map.of(
                        "role",  msg.getRole(),   // "user" hoặc "model"
                        "parts", List.of(Map.of("text", msg.getContent()))
                    ));
                }
            }

            // Thêm tin nhắn hiện tại
            contents.add(Map.of(
                "role",  "user",
                "parts", List.of(Map.of("text", userMessage))
            ));

            // ── Build payload theo Gemini API format ─────────────────────────
            Map<String, Object> payload = Map.of(
                "system_instruction", Map.of(
                    "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", contents,
                "generationConfig", Map.of(
                    "temperature",     0.7,
                    "maxOutputTokens", 1024,
                    "topP",            0.9
                )
            );

            String fullUrl = apiUrl + "?key=" + apiKey;

            log.debug("Calling Gemini API with {} history messages + 1 new message",
                    history != null ? history.size() : 0);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post()
                    .uri(fullUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return extractText(response);

        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi kết nối AI: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "Xin lỗi, mình không nhận được phản hồi từ AI. Vui lòng thử lại!";
            }
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            return "Xin lỗi, mình gặp lỗi xử lý phản hồi. Vui lòng thử lại!";
        }
    }
}
