package com.sportcenter.sportcenter.payment.dto;

import java.time.LocalDateTime;

import com.sportcenter.sportcenter.member.entity.Member;
import com.sportcenter.sportcenter.venue.entity.BookingVenues;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookingVenuesResponseDTO {
    private Long bookingVenuesId;
    private String venuesCategory;
    private String venuesUnitName;
    private String venuesNo;
    private String openDate;
    private String timeSlotName;
    private String venuesStatus;
    private Integer venuesPrice;
    private String bookingStatus;
    private String bookingId;
    private LocalDateTime orderTime;
    private Member member; // 改用 Member 實體類

        // 使用 BookingVenues 來初始化 DTO
    public BookingVenuesResponseDTO(BookingVenues booking) {
        this.bookingVenuesId = booking.getBookingVenuesId();
        this.venuesCategory = booking.getVenuesCategory();
        this.venuesUnitName = booking.getVenuesUnitName();
        this.venuesNo = booking.getVenuesNo();
        this.openDate = booking.getOpenDate().toString(); // 假設 openDate 是 LocalDateTime 類型
        this.timeSlotName = booking.getTimeSlotName();
        this.venuesStatus = booking.getVenuesStatus();
        this.venuesPrice = booking.getVenuesPrice();
        this.bookingStatus = booking.getBookingStatus();
        this.bookingId = booking.getBookingId();
        this.orderTime = booking.getOrderTime();
        this.member = booking.getMember(); // 假設 BookingVenues 有 member 欄位
    }
}
