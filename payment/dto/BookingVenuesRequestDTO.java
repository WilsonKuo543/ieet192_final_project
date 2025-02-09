package com.sportcenter.sportcenter.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookingVenuesRequestDTO {
    private String venuesCategory;
    private String venuesUnitName;
    private String venuesNo;
    private String openDate;
    private String timeSlotName;
    private String venuesStatus;
    private Integer venuesPrice;
    private String bookingStatus;
    private String bookingId;
    private Long memberId; // 只傳送 ID 而不是整個 Member 對象
}
