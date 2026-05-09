package com.lms.library.presentation.controller;

import com.lms.library.application.dto.ChatRequest;
import com.lms.library.application.dto.ChatResponse;
import com.lms.library.application.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Chat", description = "Chatbot AI hỗ trợ độc giả và thủ thư")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    @Operation(
        summary = "Gửi tin nhắn đến AI trợ lý thư viện",
        description = "AI tự động nhận dữ liệu thực tế (sách đang mượn, phí phạt) " +
                      "từ tài khoản người dùng và trả lời phù hợp theo vai trò."
    )
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("POST /api/v1/chat from user: {}", userDetails.getUsername());
        ChatResponse response = chatService.chat(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
