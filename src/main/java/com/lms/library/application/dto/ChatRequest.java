package com.lms.library.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatRequest {

    @NotBlank(message = "Tin nhắn không được để trống")
    @Size(max = 1000, message = "Tin nhắn không được vượt quá 1000 ký tự")
    private String message;

    /**
     * Lịch sử hội thoại từ Frontend gửi lên để AI nhớ ngữ cảnh.
     * Mỗi phần tử gồm role ("user" | "model") và content.
     */
    @Size(max = 20, message = "Lịch sử tối đa 20 tin nhắn")
    private List<ChatMessage> history = new ArrayList<>();

    @Data
    public static class ChatMessage {
        /** "user" hoặc "model" (Gemini dùng "model" thay vì "assistant") */
        private String role;
        private String content;
    }
}
