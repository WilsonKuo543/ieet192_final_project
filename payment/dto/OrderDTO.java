package com.sportcenter.sportcenter.payment.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.sportcenter.sportcenter.product.dto.OrderItemDTO;
import com.sportcenter.sportcenter.product.entity.Order.PickupType;

import lombok.Data;

@Data
public class OrderDTO {
    private Integer id;
    private Integer memberId;
    private String memberName;
    private String phone;
    private String email;
    private PickupType pickupType;
    private String address;
    private Integer total;
    private Boolean payStatus;
    private Boolean pickStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<OrderItemDTO> items;
    private String bookingId;

}
