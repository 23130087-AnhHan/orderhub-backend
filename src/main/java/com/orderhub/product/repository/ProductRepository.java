package com.orderhub.product.repository;

import com.orderhub.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatus(String status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndStatus(String keyword, String status, Pageable pageable);

    Optional<Product> findByIdAndStatus(Long id, String status);
}