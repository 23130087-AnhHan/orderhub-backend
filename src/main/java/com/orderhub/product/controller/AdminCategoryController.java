package com.orderhub.product.controller;

import com.orderhub.common.response.ApiResponse;
import com.orderhub.product.dto.CategoryRequest;
import com.orderhub.product.dto.CategoryResponse;
import com.orderhub.product.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ApiResponse.success("Category created successfully", response);
    }
}