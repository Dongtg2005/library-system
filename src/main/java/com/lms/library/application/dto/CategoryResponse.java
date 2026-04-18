package com.lms.library.application.dto;

import com.lms.library.domain.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private List<CategoryResponse> children;

    public static CategoryResponse from(Category category) {
        if (category == null) return null;
        
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(category.getChildren() != null ? 
                        category.getChildren().stream().map(CategoryResponse::from).collect(Collectors.toList()) 
                        : null)
                .build();
    }
}
