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
public class SystemSetting {
    
    private String key;
    private String value;
    private String description;
    private DataType dataType;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    
    public enum DataType {
        STRING, NUMBER, BOOLEAN, JSON
    }
    
    public String getAsString() {
        return value;
    }
    
    public Integer getAsInteger() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Boolean getAsBoolean() {
        return Boolean.parseBoolean(value);
    }
}
