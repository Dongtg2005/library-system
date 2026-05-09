package com.lms.library.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String reply;
    private boolean success;
    private String error;

    public static ChatResponse success(String reply) {
        return ChatResponse.builder()
                .reply(reply)
                .success(true)
                .build();
    }

    public static ChatResponse error(String error) {
        return ChatResponse.builder()
                .success(false)
                .error(error)
                .reply("Xin lỗi, mình gặp sự cố kỹ thuật. Bạn vui lòng thử lại sau nhé!")
                .build();
    }
}
