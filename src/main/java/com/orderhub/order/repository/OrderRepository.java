package com.orderhub.order.repository;

import com.orderhub.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    boolean existsByOrderCode(String orderCode);
}