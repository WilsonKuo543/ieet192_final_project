package com.sportcenter.sportcenter.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportcenter.sportcenter.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer>, PaymentDAO {
    Payment findByMerchantTradeNo(String merchantTradeNo);
}
