package com.sportcenter.sportcenter.payment.dto;

import java.util.List;

import com.sportcenter.sportcenter.course.entity.CourseOrder;



public record OrdersResponse(
                long count, // 記錄數量
                List<CourseOrder> list, // CourseOrder 列表
                boolean success, // 是否成功
                String message) { // 回傳的訊息
}