package com.sportcenter.sportcenter.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportcenter.sportcenter.product.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // 可以根據會員ID查詢訂單
    List<Order> findByMemberId(Integer memberId);

    // 根據 bookingId 查詢訂單
    Optional<Order> findByBookingId(String bookingId);

    // 使用 LIKE 进行模糊查询
    List<Order> findByBookingIdContaining(String bookingId);
}