package com.lms.library.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequest {
    
    @NotNull(message = "Book ID is required")
    private UUID bookId;
    
    private Integer priority; // 1=Normal, 2=High, 3=Urgent
    
    private String notes;
}
