package com.lms.library.application.service;

import com.lms.library.application.dto.CategoryRequest;
import com.lms.library.application.dto.CategoryResponse;
import com.lms.library.domain.entity.Category;
import com.lms.library.domain.exception.DuplicateResourceException;
import com.lms.library.domain.exception.ResourceNotFoundException;
import com.lms.library.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesTree() {
        return categoryRepository.findByParentIsNull().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesFlat() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category name already exists: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentId()));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.from(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (!category.getName().equalsIgnoreCase(request.getName()) && 
            categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category name already exists: " + request.getName());
        }

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("A category cannot be its own parent.");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        return CategoryResponse.from(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (category.hasChildren()) {
            throw new IllegalStateException("Cannot delete category because it has subcategories. Remove children first.");
        }
        
        // TODO: In the future, check if category is linked to books before deleting

        categoryRepository.delete(category);
    }
}
