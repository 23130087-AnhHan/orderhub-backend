package com.orderhub.product.service;

import com.orderhub.common.exception.BusinessException;
import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.common.response.PageResponse;
import com.orderhub.product.dto.ProductFilterRequest;
import com.orderhub.product.dto.ProductRequest;
import com.orderhub.product.dto.ProductResponse;
import com.orderhub.product.dto.ProductVariantRequest;
import com.orderhub.product.entity.Category;
import com.orderhub.product.entity.Product;
import com.orderhub.product.mapper.ProductMapper;
import com.orderhub.product.repository.ProductRepository;
import com.orderhub.product.repository.ProductVariantRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            CategoryService categoryService,
            ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.categoryService = categoryService;
        this.productMapper = productMapper;
    }

    /*
     * Create a new product with variants.
     *
     * Cache rule:
     * When admin creates a product, product list/detail cache must be cleared.
     * Otherwise, users may still see old cached product data.
     */
    @Transactional
    @CacheEvict(value = {"products", "product-detail"}, allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryService.getActiveCategoryEntity(request.getCategoryId());

        validateSkuNotDuplicated(request);

        Product product = productMapper.toEntity(request, category);
        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    /*
     * Get product list with pagination and optional filters.
     *
     * Cache rule:
     * The result is cached by page, size, keyword, categoryId and status.
     * If the same request is called again, data can be returned from Redis
     * instead of querying MySQL again.
     */
    @Cacheable(
            value = "products",
            key = "'page=' + #request.page + ':size=' + #request.size + ':keyword=' + #request.keyword + ':categoryId=' + #request.categoryId + ':status=' + #request.status"
    )
    public PageResponse<ProductResponse> getProducts(ProductFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        String status = request.getStatus() == null ? "ACTIVE" : request.getStatus();

        Page<Product> productPage;

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndStatus(
                    request.getKeyword(),
                    status,
                    pageable
            );
        } else if (request.getCategoryId() != null) {
            productPage = productRepository.findByCategoryIdAndStatus(
                    request.getCategoryId(),
                    status,
                    pageable
            );
        } else {
            productPage = productRepository.findByStatus(status, pageable);
        }

        return PageResponse.of(
                productPage.getContent()
                        .stream()
                        .map(productMapper::toResponse)
                        .toList(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }

    /*
     * Get product detail by product ID.
     *
     * Cache rule:
     * Each product detail is cached by product ID.
     * Example cache key:
     * product-detail::1
     */
    @Cacheable(value = "product-detail", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return productMapper.toResponse(product);
    }

    /*
     * Update product information.
     *
     * Cache rule:
     * When admin updates a product, all product-related cache should be cleared.
     */
    @Transactional
    @CacheEvict(value = {"products", "product-detail"}, allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Category category = categoryService.getActiveCategoryEntity(request.getCategoryId());

        productMapper.updateEntity(product, request, category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    /*
     * Soft delete product by changing status to INACTIVE.
     *
     * Cache rule:
     * When admin deletes a product, product cache must be cleared.
     */
    @Transactional
    @CacheEvict(value = {"products", "product-detail"}, allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setStatus("INACTIVE");
        productRepository.save(product);
    }

    /*
     * Validate SKU before creating product.
     *
     * SKU must be unique because it identifies one specific product variant.
     * Example:
     * TSHIRT-WHITE-M
     */
    private void validateSkuNotDuplicated(ProductRequest request) {
        if (request.getVariants() == null) {
            return;
        }

        for (ProductVariantRequest variantRequest : request.getVariants()) {
            if (productVariantRepository.existsBySku(variantRequest.getSku())) {
                throw new BusinessException("SKU already exists: " + variantRequest.getSku());
            }
        }
    }
}