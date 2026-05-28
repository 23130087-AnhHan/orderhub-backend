package com.orderhub.product.service;

import com.orderhub.common.exception.BusinessException;
import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.product.dto.CategoryRequest;
import com.orderhub.product.dto.CategoryResponse;
import com.orderhub.product.entity.Category;
import com.orderhub.product.mapper.CategoryMapper;
import com.orderhub.product.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Category name already exists");
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAll()
                .stream()
                .filter(category -> "ACTIVE".equals(category.getStatus()))
                .map(categoryMapper::toResponse)
                .toList();
    }

    public Category getActiveCategoryEntity(Long id) {
        return categoryRepository.findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}