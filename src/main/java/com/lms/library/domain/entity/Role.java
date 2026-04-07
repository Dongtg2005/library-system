package com.lms.library.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    private Long id;
    private String name;
    private String description;
    private Map<String, Boolean> permissions;
    private LocalDateTime createdAt;
    
    public enum RoleName {
        GUEST("Guest", Map.of(
            "READ_BOOKS", true,
            "VIEW_BOOK_DETAILS", true
        )),
        USER("User", Map.of(
            "READ_BOOKS", true,
            "VIEW_BOOK_DETAILS", true,
            "BORROW_BOOKS", true,
            "RETURN_BOOKS", true,
            "RATE_BOOKS", true,
            "MANAGE_PROFILE", true,
            "VIEW_HISTORY", true
        )),
        LIBRARIAN("Librarian", Map.ofEntries(
            Map.entry("READ_BOOKS", true),
            Map.entry("VIEW_BOOK_DETAILS", true),
            Map.entry("BORROW_BOOKS", true),
            Map.entry("RETURN_BOOKS", true),
            Map.entry("RATE_BOOKS", true),
            Map.entry("MANAGE_PROFILE", true),
            Map.entry("VIEW_HISTORY", true),
            Map.entry("MANAGE_BOOKS", true),
            Map.entry("MANAGE_USERS", true),
            Map.entry("APPROVE_BORROWS", true),
            Map.entry("VIEW_REPORTS", true)
        )),
        ADMIN("Administrator", Map.of(
            "ALL_PERMISSIONS", true
        ));
        
        private final String displayName;
        private final Map<String, Boolean> permissions;
        
        RoleName(String displayName, Map<String, Boolean> permissions) {
            this.displayName = displayName;
            this.permissions = permissions;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Map<String, Boolean> getPermissions() {
            return permissions;
        }
    }
}
