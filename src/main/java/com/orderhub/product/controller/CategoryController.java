package com.orderhub.product.controller;

import com.orderhub.common.response.ApiResponse;
import com.orderhub.product.dto.CategoryResponse;
import com.orderhub.product.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/v1/categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        List<CategoryResponse> response = categoryService.getActiveCategories();
        return ApiResponse.success("Categories retrieved successfully", response);
    }
}