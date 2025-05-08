package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderIdAndEmail(Long orderId, String email);

    Page<Order> findByOrderStatus(String status, Pageable pageable);

    Page<Order> findByEmailContainingOrOrderIdContaining(String email, String orderId, Pageable pageable);

    Page<Order> findByOrderStatusAndEmailContainingOrOrderIdContaining(
            String status, String email, String orderId, Pageable pageable);
}