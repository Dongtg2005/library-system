package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavorite {
    private Long userId;
    private UUID bookId;
    private LocalDateTime createdAt;
    
    // Composite key
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFavorite)) return false;
        UserFavorite that = (UserFavorite) o;
        return userId.equals(that.userId) && bookId.equals(that.bookId);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(userId, bookId);
    }
}
