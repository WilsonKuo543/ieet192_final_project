package com.sportcenter.sportcenter.product.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "items"}, allowGetters = true)
public class Order{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

    @NotNull(message = "會員ID不能為空")
    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @NotBlank(message = "訂購人姓名不能為空")
    @Column(name = "member_name", length = 50, nullable = false)
    private String memberName;

    @NotBlank(message = "電話不能為空")
    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @NotBlank(message = "Email不能為空")
    @Email(message = "Email格式不正確")
    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @NotNull(message = "取貨方式不能為空")
    @Column(name = "pickup_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PickupType pickupType;

    @Column(name = "address", length = 255)
    private String address;

    @NotNull(message = "總金額不能為空")
    @Min(value = 0, message = "總金額不能小於0")
    @Column(name = "total", nullable = false)
    private Integer total = 0;

    @Column(name = "pay_status", nullable = false)
    private Boolean payStatus = false;

    @Column(name = "pick_status", nullable = false)
    private Boolean pickStatus = false;
    
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    @Column(name = "booking_id")
    private String bookingId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("order")
    private List<OrderItem> items = new ArrayList<>();

    /*取貨方式枚舉*/
    public enum PickupType {
        STORE,      // 門市自取
        DELIVERY    // 宅配
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
        validateAddress();
    }

    private void validateAddress() {
        if (pickupType == PickupType.DELIVERY && (address == null || address.trim().isEmpty())) {
            throw new IllegalStateException("宅配訂單必須填寫地址");
        }
    }
}
