package com.orderhub.product.controller;

import com.orderhub.common.response.ApiResponse;
import com.orderhub.common.response.PageResponse;
import com.orderhub.product.dto.ProductFilterRequest;
import com.orderhub.product.dto.ProductResponse;
import com.orderhub.product.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getProducts(ProductFilterRequest request) {
        PageResponse<ProductResponse> response = productService.getProducts(request);
        return ApiResponse.success("Products retrieved successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ApiResponse.success("Product retrieved successfully", response);
    }
}