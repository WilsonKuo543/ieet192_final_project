package com.sportcenter.sportcenter.payment.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private Integer normId;
    private String productName;
    private String normName;
    private Integer price;
    private Integer quantity;
    private Integer subtotal;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

