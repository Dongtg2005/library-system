package com.lms.library.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Boolean> permissions;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Role constants
    public static final String ROLE_GUEST = "GUEST";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_LIBRARIAN = "LIBRARIAN";
    public static final String ROLE_ADMIN = "ADMIN";

    // Default permissions for each role
    public static final Map<String, Boolean> GUEST_PERMISSIONS = Map.of(
        "READ_BOOKS", true,
        "VIEW_BOOK_DETAILS", true
    );

    public static final Map<String, Boolean> USER_PERMISSIONS = Map.of(
        "READ_BOOKS", true,
        "VIEW_BOOK_DETAILS", true,
        "BORROW_BOOKS", true,
        "RETURN_BOOKS", true,
        "RATE_BOOKS", true,
        "MANAGE_PROFILE", true,
        "VIEW_HISTORY", true
    );

    public static final Map<String, Boolean> LIBRARIAN_PERMISSIONS = Map.ofEntries(
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
    );

    public static final Map<String, Boolean> ADMIN_PERMISSIONS = Map.of(
        "ALL_PERMISSIONS", true
    );

    public boolean hasPermission(String permission) {
        return permissions != null && Boolean.TRUE.equals(permissions.get(permission));
    }
}
