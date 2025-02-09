package com.sportcenter.sportcenter.payment.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportcenter.sportcenter.course.entity.Course;
import com.sportcenter.sportcenter.course.entity.CourseOrder;
import com.sportcenter.sportcenter.course.service.CourseService;
import com.sportcenter.sportcenter.payment.dto.BookingVenuesRequestDTO;
import com.sportcenter.sportcenter.payment.dto.BookingVenuesResponseDTO;
import com.sportcenter.sportcenter.payment.dto.OrderDTO;
import com.sportcenter.sportcenter.payment.dto.OrdersResponse;
import com.sportcenter.sportcenter.payment.service.OrderService;
import com.sportcenter.sportcenter.payment.service.PaymentService;
import com.sportcenter.sportcenter.venue.entity.BookingVenues;



@RestController
@RequestMapping("/orders")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    // ===================== 課程訂單相關 API =====================//

    // 查詢訂單列表
    @PostMapping("/find")
    public OrdersResponse find(@RequestBody String entity) {
        long count = orderService.count(entity);
        List<CourseOrder> orders = orderService.find(entity);
        return new OrdersResponse(count, orders, true, "成功取得訂單列表");
    }

    // 查找單筆訂單 （通過 courseOrderId）
    @GetMapping("/find/{courseOrderId}")
    public OrdersResponse findById(@PathVariable Integer courseOrderId) {
        List<CourseOrder> orders = new ArrayList<>();
        if (courseOrderId != null) {
            CourseOrder order = orderService.findById(courseOrderId);
            if (order != null) {
                orders.add(order);
            }
        }
        return new OrdersResponse(orders.size(), orders, true, "成功取得訂單");
    }

    // 刪除訂單（通過 courseOrderId）
    @DeleteMapping("/delete/{courseOrderId}")
    public OrdersResponse removeByBookingId(@PathVariable Integer courseOrderId) {
        if (courseOrderId == null) {
            return new OrdersResponse(0, null, false, "訂單編號是必要欄位");
        }
        CourseOrder order = orderService.findById(courseOrderId);
        if (order == null) {
            return new OrdersResponse(0, null, false, "訂單編號不存在");
        }
        if (orderService.remove(order.getCourseOrderId())) {
            return new OrdersResponse(0, null, true, "刪除成功");
        }
        return new OrdersResponse(0, null, false, "刪除失敗");
    }

    // 修改訂單（通過 courseOrderId）
    @PutMapping("/update/{courseOrderId}")
    public OrdersResponse modifyBycourseOrderId(@PathVariable Integer courseOrderId, @RequestBody String entity) {
        if (courseOrderId == null) {
            return new OrdersResponse(0, null, false, "訂單編號是必要欄位");
        }
        CourseOrder order = orderService.findById(courseOrderId);
        if (order == null) {
            return new OrdersResponse(0, null, false, "訂單編號不存在");
        }
        CourseOrder update = orderService.modify(entity);
        if (update == null) {
            return new OrdersResponse(0, null, false, "修改失敗");
        }
        return new OrdersResponse(1, List.of(update), true, "修改成功");
    }

    // 會員取消訂單（通過 bookingId，連動payment table，直接用bookingId比較方便）
    @PutMapping("/cancel/{bookingId}")
    public OrdersResponse memberCancelOrderByBookingId(@PathVariable String bookingId) {
        if (bookingId == null) {
            return new OrdersResponse(0, null, false, "訂單編號是必要欄位");
        }
        CourseOrder cancelledOrder = orderService.cancelOrderByBookingId(bookingId);
        if (cancelledOrder != null) {

            if (paymentService.getPaymentByOrderId(bookingId) != null) {
                paymentService.updatePaymentStatus(bookingId, "已取消",
                        paymentService.getPaymentByOrderId(bookingId).getMerchantTradeNo());
            }

            return new OrdersResponse(1, List.of(cancelledOrder), true, "訂單已成功取消");
        }
        return new OrdersResponse(0, null, false, "取消訂單失敗");
    }

    // ===================== 場地訂單相關 API =====================//

        /**
     * 會員用：根據 memberId 和 bookingId 查詢訂單
     * 
     * @param bookingId 訂單編號（可部分匹配）
     * @param memberId  會員ID
     */
    @GetMapping("/venues/booking/member/search")
    public ResponseEntity<List<BookingVenuesResponseDTO>> searchMemberBookings(
            @RequestParam String bookingId,
            @RequestParam String memberId) {

        // 轉換 memberId 為 Integer 類型
        Integer memberIdInteger = Integer.parseInt(memberId);

        // 查詢會員的訂單，根據 memberId 和 bookingId 模糊匹配
        List<BookingVenues> bookings = orderService.searchByMemberIdAndBookingId(memberIdInteger, bookingId);

        // 可以進行 DTO 轉換，將實體轉為回應所需的格式
        List<BookingVenuesResponseDTO> response = bookings.stream()
                .map(booking -> new BookingVenuesResponseDTO(booking)) // 假設有轉換邏輯
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 根據 ID 查詢場地訂單
     * 
     * @param id 場地訂單 ID
     */
    @GetMapping("/venues/booking/{id}")
    public ResponseEntity<BookingVenuesResponseDTO> getVenueBooking(@PathVariable Long id) {
        BookingVenuesResponseDTO booking = orderService.findVenueById(id);
        if (booking != null) {
            return ResponseEntity.ok(booking);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 模糊搜尋場地訂單（根據 bookingId）
     * 
     * @param bookingId 訂單編號（可部分匹配）
     */
    @GetMapping("/venues/booking/search")
    public ResponseEntity<List<BookingVenuesResponseDTO>> searchVenueBookings(@RequestParam String bookingId) {
        List<BookingVenuesResponseDTO> bookings = orderService.searchByBookingId(bookingId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * 取消場地訂單
     * 
     * @param bookingId 場地訂單編號
     */
    @PutMapping("/venues/booking/cancel/{bookingId}")
    public ResponseEntity<BookingVenuesResponseDTO> cancelVenueBooking(@PathVariable String bookingId) {
        if (bookingId == null) {
            return ResponseEntity.badRequest().build();
        }

        BookingVenuesResponseDTO cancelledBooking = orderService.cancelVenueBooking(bookingId);
        if (cancelledBooking != null) {
            // 如果需要更新支付狀態，可以參考課程訂單的做法
            // if (paymentService.getPaymentByOrderId(bookingId) != null) {
            // paymentService.updatePaymentStatus(bookingId, "已取消",
            // paymentService.getPaymentByOrderId(bookingId).getMerchantTradeNo());
            // }
            return ResponseEntity.ok(cancelledBooking);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/venues/booking/delete/{bookingId}")
    public ResponseEntity<BookingVenuesResponseDTO> deleteVenueBooking(@PathVariable String bookingId) {
        boolean deleted = orderService.deleteVenueBooking(bookingId);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/venues/booking/update/{bookingId}")
    public ResponseEntity<BookingVenuesResponseDTO> updateVenueBooking(
            @PathVariable String bookingId,
            @RequestBody BookingVenuesRequestDTO requestDTO) {
        if (bookingId == null) {
            return ResponseEntity.badRequest().build();
        }
        BookingVenuesResponseDTO updatedBooking = orderService.updateVenueBooking(bookingId, requestDTO);
        if (updatedBooking != null) {
            return ResponseEntity.ok(updatedBooking);
        }
        return ResponseEntity.notFound().build();
    }

    // ======================購物車訂單相關 API======================//
    @GetMapping("/shopping/search")
    public ResponseEntity<List<OrderDTO>> searchOrders(@RequestParam(required = false) String bookingId) {
        List<OrderDTO> orders = orderService.searchOrders(bookingId); // 這裡會返回 List<OrderDTO>
        return ResponseEntity.ok(orders);
    }

    /**
     * 根據 booking_id 查詢訂單
     */
    @GetMapping("/shopping/booking/{bookingId}")
    public ResponseEntity<OrderDTO> getOrderByBookingId(@PathVariable String bookingId) {
        // 需要在 OrderService 中新增這個方法
        OrderDTO order = orderService.getOrderByBookingId(bookingId);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 查詢單一購物車訂單
     */
    @GetMapping("/shopping/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Integer orderId) {
        OrderDTO order = orderService.getOrder(orderId);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 查詢會員的所有購物車訂單
     */
    @GetMapping("/shopping/member/{memberId}")
    public ResponseEntity<List<OrderDTO>> getMemberOrders(@PathVariable Integer memberId) {
        List<OrderDTO> orders = orderService.getMemberOrders(memberId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 更新購物車訂單狀態（後台）
     */
    @PutMapping("/shopping/{bookingId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable String bookingId, // 改為接收 bookingId
            @RequestParam(required = false) Boolean payStatus,
            @RequestParam(required = false) Boolean pickStatus,
            @RequestParam(required = false) Integer total) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(bookingId, payStatus, pickStatus);
        if (updatedOrder != null) {
            return ResponseEntity.ok(updatedOrder);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 取消訂單（前台用戶）
     */
    @PutMapping("/shopping/{bookingId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable String bookingId) {
        OrderDTO cancelledOrder = orderService.updateOrderStatus(bookingId, false, false);
        if (cancelledOrder != null) {
            return ResponseEntity.ok(cancelledOrder);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 刪除訂單（後台）
     */
    @DeleteMapping("/shopping/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer orderId) {
        // 這個方法需要在 OrderService 中添加
        boolean deleted = orderService.deleteOrder(orderId);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/shopping/{orderId}/details")
    public ResponseEntity<OrderDTO> getOrderDetails(@PathVariable("orderId") Integer orderId) {
        try {
            OrderDTO orderDetails = orderService.getOrderDetails(orderId);
            if (orderDetails != null) {
                return ResponseEntity.ok(orderDetails);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OrderDTO());
        }
    }

    @Autowired
    private CourseService courseService;

    @GetMapping("/course/find/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Integer id) {
        System.out.println("Received courseId: " + id); // 加入這行
        Course course = courseService.findCourseById(id);
        if (course != null) {
            return ResponseEntity.ok(course);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}