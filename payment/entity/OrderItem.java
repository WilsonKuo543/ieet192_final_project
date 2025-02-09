package com.sportcenter.sportcenter.product.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
@Entity
@Table(name = "order_items")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler","order"})
public class OrderItem {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@NotNull(message = "訂單不能為空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
	@JsonIgnoreProperties("items")
    private Order order;

    @NotNull(message = "商品ID不能為空")
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "norm_id")
    private Integer normId;

    @NotBlank(message = "商品名稱不能為空")
    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Column(name = "norm_name", length = 50)
    private String normName;

    @NotNull(message = "價格不能為空")
    @Min(value = 0, message = "價格不能小於0")
    @Column(name = "price", nullable = false)
    private Integer price;

    @NotNull(message = "數量不能為空")
    @Min(value = 1, message = "數量必須大於0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "小計不能為空")
    @Min(value = 0, message = "小計不能小於0")
    @Column(name = "subtotal", nullable = false)
    private Integer subtotal;
    
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /* 計算小計金額*/
    public void calculateSubtotal() {
        this.subtotal = this.price * this.quantity;
    }
}