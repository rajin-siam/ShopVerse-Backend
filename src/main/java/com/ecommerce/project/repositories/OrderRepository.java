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

    Page<Order> findByOrderStatus(String orderStatus, Pageable pageable);

    Page<Order> findByEmailContaining(String email, Pageable pageable);

    Page<Order> findByOrderStatusAndEmailContaining(String orderStatus, String email, Pageable pageable);

    Page<Order> findByEmailContainingOrOrderId(String email, Long orderId, Pageable pageable);

    Page<Order> findByOrderStatusAndEmailContainingOrOrderStatusAndOrderId(
            String orderStatus1, String email, String orderStatus2, Long orderId, Pageable pageable);
}