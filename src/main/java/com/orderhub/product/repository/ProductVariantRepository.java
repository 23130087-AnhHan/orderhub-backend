package com.orderhub.product.repository;

import com.orderhub.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    boolean existsBySku(String sku);

    Optional<ProductVariant> findByIdAndStatus(Long id, String status);

    List<ProductVariant> findByProductIdAndStatus(Long productId, String status);
}