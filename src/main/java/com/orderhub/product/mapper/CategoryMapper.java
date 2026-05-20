package com.orderhub.product.mapper;

import com.orderhub.product.dto.CategoryRequest;
import com.orderhub.product.dto.CategoryResponse;
import com.orderhub.product.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
        return category;
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getStatus(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public void updateEntity(Category category, CategoryRequest request) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
    }
}