package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    private Long id;
    private String name;
    private String color; // Hex color for UI
    private LocalDateTime createdAt;
    
    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
