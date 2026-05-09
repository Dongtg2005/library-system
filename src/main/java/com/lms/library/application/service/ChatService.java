package com.lms.library.application.service;

import com.lms.library.application.dto.ChatRequest;
import com.lms.library.application.dto.ChatResponse;
import com.lms.library.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private static final int MAX_REQUESTS_PER_SESSION = 20;

    private final GeminiService geminiService;
    private final ChatPromptBuilder promptBuilder;
    private final AuthenticationService authenticationService;

    /** Rate limiting: đếm số tin nhắn mỗi user theo email */
    private final Map<String, AtomicInteger> requestCount = new ConcurrentHashMap<>();

    public ChatResponse chat(String userEmail, ChatRequest request) {
        try {
            // ── 1. Rate limiting ────────────────────────────────────────────
            int count = requestCount
                    .computeIfAbsent(userEmail, k -> new AtomicInteger(0))
                    .incrementAndGet();

            if (count > MAX_REQUESTS_PER_SESSION) {
                log.warn("Rate limit exceeded for user: {}", userEmail);
                return ChatResponse.error(
                    "Bạn đã gửi quá " + MAX_REQUESTS_PER_SESSION
                    + " tin nhắn trong phiên này. Vui lòng thử lại sau!"
                );
            }

            // ── 2. Lấy user & xác định role ưu tiên cao nhất ───────────────
            User user = authenticationService.findByEmail(userEmail);

            String userRole = user.getRoles().stream()
                    .map(r -> r.getName())
                    .sorted(Comparator.comparingInt(ChatService::rolePriority).reversed())
                    .findFirst()
                    .orElse("USER");

            log.info("Chat request [{}/{}] from user: {} (role: {})",
                    count, MAX_REQUESTS_PER_SESSION, userEmail, userRole);

            // ── 3. Build system prompt với dữ liệu thực tế từ DB ───────────
            String systemPrompt = promptBuilder.buildSystemPrompt(user, userRole);

            // ── 4. Gọi Gemini kèm history để AI nhớ ngữ cảnh ──────────────
            String aiReply = geminiService.chat(systemPrompt, request.getHistory(), request.getMessage());

            return ChatResponse.success(aiReply);

        } catch (Exception e) {
            log.error("Chat service error for user {}: {}", userEmail, e.getMessage(), e);
            return ChatResponse.error(e.getMessage());
        }
    }

    /** Ưu tiên role: ADMIN > LIBRARIAN > USER */
    private static int rolePriority(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN"     -> 3;
            case "LIBRARIAN" -> 2;
            default          -> 1;
        };
    }

    /** Reset rate limit counter (gọi khi user logout hoặc sau 1 khoảng thời gian) */
    public void resetRateLimit(String userEmail) {
        requestCount.remove(userEmail);
    }
}
