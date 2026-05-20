package com.orderhub.product.mapper;

import com.orderhub.product.dto.ProductRequest;
import com.orderhub.product.dto.ProductResponse;
import com.orderhub.product.dto.ProductVariantRequest;
import com.orderhub.product.dto.ProductVariantResponse;
import com.orderhub.product.entity.Category;
import com.orderhub.product.entity.Product;
import com.orderhub.product.entity.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    public ProductMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public Product toEntity(ProductRequest request, Category category) {
        Product product = new Product();
        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setThumbnailUrl(request.getThumbnailUrl());
        product.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());

        if (request.getVariants() != null) {
            for (ProductVariantRequest variantRequest : request.getVariants()) {
                ProductVariant variant = toVariantEntity(variantRequest);
                product.addVariant(variant);
            }
        }

        return product;
    }

    public ProductVariant toVariantEntity(ProductVariantRequest request) {
        ProductVariant variant = new ProductVariant();
        variant.setSku(request.getSku());
        variant.setColor(request.getColor());
        variant.setSize(request.getSize());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setPrice(request.getPrice());
        variant.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
        return variant;
    }

    public ProductResponse toResponse(Product product) {
        List<ProductVariantResponse> variantResponses = product.getVariants()
                .stream()
                .map(this::toVariantResponse)
                .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getThumbnailUrl(),
                product.getStatus(),
                categoryMapper.toResponse(product.getCategory()),
                variantResponses,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public ProductVariantResponse toVariantResponse(ProductVariant variant) {
        return new ProductVariantResponse(
                variant.getId(),
                variant.getSku(),
                variant.getColor(),
                variant.getSize(),
                variant.getStockQuantity(),
                variant.getPrice(),
                variant.getStatus()
        );
    }

    public void updateEntity(Product product, ProductRequest request, Category category) {
        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setThumbnailUrl(request.getThumbnailUrl());
        product.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
    }
}