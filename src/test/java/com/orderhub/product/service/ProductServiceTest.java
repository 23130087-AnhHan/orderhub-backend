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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Product product;
    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private ProductVariantRequest variantRequest;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("T-Shirts");
        category.setStatus("ACTIVE");

        product = new Product();
        product.setId(1L);
        product.setCategory(category);
        product.setName("Basic T-Shirt");
        product.setDescription("A basic cotton T-shirt");
        product.setPrice(new BigDecimal("199000"));
        product.setThumbnailUrl("https://example.com/tshirt.jpg");
        product.setStatus("ACTIVE");

        variantRequest = new ProductVariantRequest();
        variantRequest.setSku("TSHIRT-WHITE-M-001");
        variantRequest.setColor("White");
        variantRequest.setSize("M");
        variantRequest.setStockQuantity(20);
        variantRequest.setPrice(new BigDecimal("199000"));
        variantRequest.setStatus("ACTIVE");

        productRequest = new ProductRequest();
        productRequest.setCategoryId(1L);
        productRequest.setName("Basic T-Shirt");
        productRequest.setDescription("A basic cotton T-shirt");
        productRequest.setPrice(new BigDecimal("199000"));
        productRequest.setThumbnailUrl("https://example.com/tshirt.jpg");
        productRequest.setStatus("ACTIVE");
        productRequest.setVariants(List.of(variantRequest));

        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Basic T-Shirt");
        productResponse.setDescription("A basic cotton T-shirt");
        productResponse.setPrice(new BigDecimal("199000"));
        productResponse.setThumbnailUrl("https://example.com/tshirt.jpg");
        productResponse.setStatus("ACTIVE");
    }

    @Test
    void createProduct_ShouldReturnProductResponse_WhenRequestIsValid() {
        // Arrange
        when(categoryService.getActiveCategoryEntity(productRequest.getCategoryId()))
                .thenReturn(category);

        when(productVariantRepository.existsBySku(variantRequest.getSku()))
                .thenReturn(false);

        when(productMapper.toEntity(productRequest, category))
                .thenReturn(product);

        when(productRepository.save(product))
                .thenReturn(product);

        when(productMapper.toResponse(product))
                .thenReturn(productResponse);

        // Act
        ProductResponse response = productService.createProduct(productRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Basic T-Shirt", response.getName());
        assertEquals(new BigDecimal("199000"), response.getPrice());
        assertEquals("ACTIVE", response.getStatus());

        verify(categoryService).getActiveCategoryEntity(productRequest.getCategoryId());
        verify(productVariantRepository).existsBySku(variantRequest.getSku());
        verify(productMapper).toEntity(productRequest, category);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void createProduct_ShouldThrowBusinessException_WhenSkuAlreadyExists() {
        // Arrange
        when(categoryService.getActiveCategoryEntity(productRequest.getCategoryId()))
                .thenReturn(category);

        when(productVariantRepository.existsBySku(variantRequest.getSku()))
                .thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.createProduct(productRequest)
        );

        assertEquals("SKU already exists: TSHIRT-WHITE-M-001", exception.getMessage());

        verify(categoryService).getActiveCategoryEntity(productRequest.getCategoryId());
        verify(productVariantRepository).existsBySku(variantRequest.getSku());

        verify(productMapper, never()).toEntity(any(ProductRequest.class), any(Category.class));
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toResponse(any(Product.class));
    }

    @Test
    void getProductById_ShouldReturnProductResponse_WhenProductExistsAndActive() {
        // Arrange
        when(productRepository.findByIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Optional.of(product));

        when(productMapper.toResponse(product))
                .thenReturn(productResponse);

        // Act
        ProductResponse response = productService.getProductById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Basic T-Shirt", response.getName());
        assertEquals("ACTIVE", response.getStatus());

        verify(productRepository).findByIdAndStatus(1L, "ACTIVE");
        verify(productMapper).toResponse(product);
    }

    @Test
    void getProductById_ShouldThrowResourceNotFoundException_WhenProductDoesNotExist() {
        // Arrange
        when(productRepository.findByIdAndStatus(999L, "ACTIVE"))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(999L)
        );

        assertEquals("Product not found", exception.getMessage());

        verify(productRepository).findByIdAndStatus(999L, "ACTIVE");
        verify(productMapper, never()).toResponse(any(Product.class));
    }

    @Test
    void getProducts_ShouldReturnPageResponse_WhenNoFilterProvided() {
        // Arrange
        ProductFilterRequest filterRequest = new ProductFilterRequest();
        filterRequest.setPage(0);
        filterRequest.setSize(10);
        filterRequest.setStatus(null);
        filterRequest.setKeyword(null);
        filterRequest.setCategoryId(null);

        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findByStatus(eq("ACTIVE"), any(Pageable.class)))
                .thenReturn(productPage);

        when(productMapper.toResponse(product))
                .thenReturn(productResponse);

        // Act
        PageResponse<ProductResponse> response = productService.getProducts(filterRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(0, response.getPage());
        assertEquals(1, response.getTotalItems());
        assertEquals("Basic T-Shirt", response.getItems().get(0).getName());

        verify(productRepository).findByStatus(eq("ACTIVE"), any(Pageable.class));
        verify(productRepository, never()).findByCategoryIdAndStatus(anyLong(), anyString(), any(Pageable.class));
        verify(productRepository, never()).findByNameContainingIgnoreCaseAndStatus(anyString(), anyString(), any(Pageable.class));
        verify(productMapper).toResponse(product);
    }

    @Test
    void updateProduct_ShouldReturnProductResponse_WhenProductExists() {
        // Arrange
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(categoryService.getActiveCategoryEntity(productRequest.getCategoryId()))
                .thenReturn(category);

        when(productRepository.save(product))
                .thenReturn(product);

        when(productMapper.toResponse(product))
                .thenReturn(productResponse);

        // Act
        ProductResponse response = productService.updateProduct(1L, productRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Basic T-Shirt", response.getName());

        verify(productRepository).findById(1L);
        verify(categoryService).getActiveCategoryEntity(productRequest.getCategoryId());
        verify(productMapper).updateEntity(product, productRequest, category);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void updateProduct_ShouldThrowResourceNotFoundException_WhenProductDoesNotExist() {
        // Arrange
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, productRequest)
        );

        assertEquals("Product not found", exception.getMessage());

        verify(productRepository).findById(999L);
        verify(categoryService, never()).getActiveCategoryEntity(anyLong());
        verify(productMapper, never()).updateEntity(any(Product.class), any(ProductRequest.class), any(Category.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldSetStatusInactive_WhenProductExists() {
        // Arrange
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        // Act
        productService.deleteProduct(1L);

        // Assert
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();

        assertEquals("INACTIVE", savedProduct.getStatus());

        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_ShouldThrowResourceNotFoundException_WhenProductDoesNotExist() {
        // Arrange
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(999L)
        );

        assertEquals("Product not found", exception.getMessage());

        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }
}