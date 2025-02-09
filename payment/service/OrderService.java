package com.sportcenter.sportcenter.payment.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportcenter.sportcenter.course.entity.Course;
import com.sportcenter.sportcenter.course.entity.CourseOrder;
import com.sportcenter.sportcenter.course.repository.CourseOrderRepository;
import com.sportcenter.sportcenter.course.repository.CourseRepository;
import com.sportcenter.sportcenter.member.repository.MemberRepository;
import com.sportcenter.sportcenter.payment.dto.BookingVenuesRequestDTO;
import com.sportcenter.sportcenter.payment.dto.BookingVenuesResponseDTO;
import com.sportcenter.sportcenter.payment.dto.OrderDTO;
import com.sportcenter.sportcenter.payment.dto.OrderItemDTO;
import com.sportcenter.sportcenter.payment.repository.OrderRepository;
import com.sportcenter.sportcenter.product.entity.Order;
import com.sportcenter.sportcenter.product.entity.OrderItem;
import com.sportcenter.sportcenter.venue.entity.BookingVenues;
import com.sportcenter.sportcenter.venue.repository.BookingVenuesRepository;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private CourseOrderRepository courseOrderRepository;

    @Autowired
    private BookingVenuesRepository venuesOrderRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository; // 購物車訂單的 Repository

    @Autowired
    private CourseRepository courseRepository;

    // ======================課程訂單相關方法======================//
    public String getCourseName(Integer courseId) { // 改為 Integer
        Course course = courseRepository.findById(courseId).orElse(null);
        return course != null ? course.getCourseName() : "未知課程";
    }

    public List<CourseOrder> find(String json) {
        try {
            logger.info("查詢訂單列表 - 參數: {}", json);
            // 由於原本的find方法不存在，改用findAll()作為替代
            // return courseOrderRepository.findAll();

            // 正確建構 JSONObject
            org.json.JSONObject obj = new org.json.JSONObject(json);
            return courseOrderRepository.find(obj);

        } catch (Exception e) {
            logger.error("查詢訂單失敗: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // public List<CourseOrder> find(String json) {
    //     try {
    //         logger.info("查詢訂單列表 - 參數: {}", json);
    //         // 由於原本的find方法不存在，改用findAll()作為替代
    //         return courseOrderRepository.findAll();
    //     } catch (Exception e) {
    //         logger.error("查詢訂單失敗: {}", e.getMessage(), e);
    //         return new ArrayList<>();
    //     }
    // }
    public long count(String json) {
        try {
            // 由於原本的count方法不存在，改用count()作為替代
            // return courseOrderRepository.count();

            org.json.JSONObject obj = new org.json.JSONObject(json);
            return courseOrderRepository.count(obj);

        } catch (Exception e) {
            logger.error("查詢訂單數量失敗: {}", e.getMessage(), e);
            return 0;
        }
    }
    // public long count(String json) {
    //     try {
    //         // 由於原本的count方法不存在，改用count()作為替代
    //         return courseOrderRepository.count();
    //     } catch (Exception e) {
    //         logger.error("查詢訂單數量失敗: {}", e.getMessage(), e);
    //         return 0;
    //     }
    // }

    public CourseOrder findById(Integer courseOrderId) {
        try {
            return courseOrderId != null ? courseOrderRepository.findById(courseOrderId).orElse(null) : null;
        } catch (Exception e) {
            logger.error("查詢訂單失敗 - courseOrderId: {} - 錯誤: {}",
                    courseOrderId, e.getMessage(), e);
            return null;
        }
    }

    public boolean exists(Integer courseOrderId) {
        return courseOrderId != null && courseOrderRepository.existsById(courseOrderId);
    }

    public CourseOrder modify(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            String bookingId = obj.isNull("bookingId") ? null : obj.getString("bookingId");
            CourseOrder order;

            if (bookingId != null) {
                order = findByBookingId(bookingId);
            } else {
                Integer courseOrderId = obj.isNull("courseOrderId") ? null : obj.getInt("courseOrderId");
                order = findById(courseOrderId);
            }

            if (order != null) {
                if (!obj.isNull("memberId")) {
                    order.setMember(memberRepository.findById(obj.getInt("memberId")).orElse(null));
                }
                if (!obj.isNull("courseId")) {
                    order.setCourse(courseRepository.findById(obj.getInt("courseId")).orElse(null));
                }
                if (!obj.isNull("bookingStatus")) {
                    order.setBookingStatus(obj.getString("bookingStatus"));
                }
                if (!obj.isNull("totalPrice")) {
                    order.setTotalPrice(obj.getInt("totalPrice"));
                }
                if (!obj.isNull("orderTime")) {
                    order.setOrderTime(LocalDateTime.parse(obj.getString("orderTime")));
                }
                return courseOrderRepository.save(order);
            }
            return null;
        } catch (Exception e) {
            logger.error("修改訂單失敗: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean remove(Integer courseOrderId) {
        try {
            if (exists(courseOrderId)) {
                courseOrderRepository.deleteById(courseOrderId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("刪除訂單失敗 - courseOrderId: {} - 錯誤: {}",
                    courseOrderId, e.getMessage(), e);
            return false;
        }
    }

    // 綠借用bookingid找訂單
    public CourseOrder findByBookingId(String bookingId) {
        logger.info("根據 bookingId 查詢訂單: {}", bookingId);
        if (bookingId != null && !bookingId.trim().isEmpty()) {
            Optional<CourseOrder> optional = courseOrderRepository.findByBookingId(bookingId);
            if (optional.isPresent()) {
                logger.info("找到訂單 - bookingId: {}", bookingId);
                return optional.get();
            }
            logger.warn("未找到訂單 - bookingId: {}", bookingId);
        }
        return null;
    }

    // 會員取消訂單靠BookingId
    public CourseOrder cancelOrderByBookingId(String bookingId) {
        logger.info("取消訂單 - bookingId: {}", bookingId);
        try {
            CourseOrder order = findByBookingId(bookingId);
            if (order != null && "待付款".equals(order.getBookingStatus())) {
                order.setBookingStatus("已取消");
                CourseOrder cancelledOrder = courseOrderRepository.save(order);
                logger.info("訂單取消成功 - bookingId: {}", bookingId);
                return cancelledOrder;
            }
            logger.warn("取消訂單失敗：訂單狀態不符 - bookingId: {}", bookingId);
            return null;
        } catch (Exception e) {
            logger.error("取消訂單失敗 - bookingId: {} - 錯誤: {}", bookingId, e.getMessage(), e);
            return null;
        }
    }

    // ======================場地訂單相關方法======================//

    // 根據 memberId 和 bookingId 查詢訂單
    public List<BookingVenues> searchByMemberIdAndBookingId(Integer memberId, String bookingId) {
        return venuesOrderRepository.findByMemberMemberIdAndBookingIdContaining(memberId, bookingId);
    }

    /**
     * 查詢場地訂單
     */
    public BookingVenuesResponseDTO findVenueById(Long bookingVenuesId) {
        try {
            logger.info("查詢場地訂單 - ID: {}", bookingVenuesId);
            Optional<BookingVenues> venue = venuesOrderRepository.findById(bookingVenuesId);
            return venue.map(this::convertToVenueDTO).orElse(null);
        } catch (Exception e) {
            logger.error("查詢場地訂單失敗 - ID: {} - 錯誤: {}", bookingVenuesId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 透過 bookingId 查詢場地訂單
     */
    public BookingVenuesResponseDTO findVenueByBookingId(String bookingId) {
        try {
            logger.info("查詢場地訂單 - BookingId: {}", bookingId);
            // 需要在 VenuesRepository 中添加相應的方法
            Optional<BookingVenues> venue = venuesOrderRepository.findByBookingId(bookingId);
            return venue.map(this::convertToVenueDTO).orElse(null);
        } catch (Exception e) {
            logger.error("查詢場地訂單失敗 - BookingId: {} - 錯誤: {}", bookingId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 取消場地訂單 (給前台使用者取消訂單用的)
     */
    public BookingVenuesResponseDTO cancelVenueBooking(String bookingId) {
        try {
            logger.info("取消場地訂單 - BookingId: {}", bookingId);
            Optional<BookingVenues> venueOpt = venuesOrderRepository.findByBookingId(bookingId);

            if (venueOpt.isPresent()) {
                BookingVenues venue = venueOpt.get();
                if ("待付款".equals(venue.getBookingStatus())) {
                    venue.setBookingStatus("已取消");
                    BookingVenues cancelledVenue = venuesOrderRepository.save(venue);
                    logger.info("場地訂單取消成功 - BookingId: {}", bookingId);
                    return convertToVenueDTO(cancelledVenue);
                }
            }
            logger.warn("取消場地訂單失敗：訂單不存在或狀態不符 - BookingId: {}", bookingId);
            return null;
        } catch (Exception e) {
            logger.error("取消場地訂單失敗 - BookingId: {} - 錯誤: {}", bookingId, e.getMessage(), e);
            return null;
        }
    }

    public List<BookingVenuesResponseDTO> searchByBookingId(String bookingId) {
        try {
            List<BookingVenues> venues = venuesOrderRepository.findByBookingIdContaining(bookingId);
            return venues.stream()
                    .map(this::convertToVenueDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("搜尋場地訂單失敗: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public boolean deleteVenueBooking(String bookingId) {
        try {
            Optional<BookingVenues> venueOpt = venuesOrderRepository.findByBookingId(bookingId);
            if (venueOpt.isPresent()) {
                venuesOrderRepository.delete(venueOpt.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("刪除場地訂單失敗 - BookingId: {} - 錯誤: {}", bookingId, e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public BookingVenuesResponseDTO updateVenueBooking(String bookingId, BookingVenuesRequestDTO requestDTO) {
        try {
            logger.info("更新場地訂單 - BookingId: {}", bookingId);
            Optional<BookingVenues> venueOpt = venuesOrderRepository.findByBookingId(bookingId);

            if (venueOpt.isEmpty()) {
                logger.warn("更新場地訂單失敗：找不到訂單 - BookingId: {}", bookingId);
                return null;
            }

            BookingVenues existingBooking = venueOpt.get();

            // 更新可修改的欄位（保留原有值如果新值為 null）
            if (requestDTO.getVenuesPrice() != null) {
                existingBooking.setVenuesPrice(requestDTO.getVenuesPrice());
            }
            if (requestDTO.getBookingStatus() != null) {
                existingBooking.setBookingStatus(requestDTO.getBookingStatus());
            }
            if (requestDTO.getVenuesStatus() != null) {
                existingBooking.setVenuesStatus(requestDTO.getVenuesStatus());
            }
            if (requestDTO.getVenuesCategory() != null) {
                existingBooking.setVenuesCategory(requestDTO.getVenuesCategory());
            }
            if (requestDTO.getVenuesUnitName() != null) {
                existingBooking.setVenuesUnitName(requestDTO.getVenuesUnitName());
            }
            if (requestDTO.getVenuesNo() != null) {
                existingBooking.setVenuesNo(requestDTO.getVenuesNo());
            }
            if (requestDTO.getOpenDate() != null) {
                existingBooking.setOpenDate(requestDTO.getOpenDate());
            }
            if (requestDTO.getTimeSlotName() != null) {
                existingBooking.setTimeSlotName(requestDTO.getTimeSlotName());
            }

            // 儲存更新
            BookingVenues updatedBooking = venuesOrderRepository.save(existingBooking);
            logger.info("場地訂單更新成功 - BookingId: {}", bookingId);

            return convertToVenueDTO(updatedBooking);

        } catch (Exception e) {
            logger.error("更新場地訂單失敗 - BookingId: {} - 錯誤: {}", bookingId, e.getMessage(), e);
            return null;
        }
    }

    // ======================購物車訂單相關方法======================//
    /**
     * 獲取所有購物車訂單
     */
    public List<OrderDTO> getAllOrders() {
        try {
            logger.info("獲取所有購物車訂單");
            List<Order> orders = orderRepository.findAll();
            return orders.stream()
                    .map(this::convertToOrderDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("獲取所有購物車訂單失敗 - 錯誤: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 查詢單一購物車訂單
     */
    public OrderDTO getOrder(Integer orderId) {
        try {
            logger.info("查詢購物車訂單 - 訂單ID: {}", orderId);
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                return convertToOrderDTO(orderOptional.get());
            }
            logger.warn("查詢購物車訂單 - 訂單不存在 - 訂單ID: {}", orderId);
            return null;
        } catch (Exception e) {
            logger.error("查詢購物車訂單失敗 - 訂單ID: {} - 錯誤: {}",
                    orderId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查詢會員的所有購物車訂單
     */
    public List<OrderDTO> getMemberOrders(Integer memberId) {
        try {
            logger.info("查詢會員的購物車訂單 - 會員ID: {}", memberId);
            List<Order> orders = orderRepository.findByMemberId(memberId);
            return orders.stream()
                    .map(this::convertToOrderDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("查詢會員購物車訂單失敗 - 會員ID: {} - 錯誤: {}",
                    memberId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新購物車訂單狀態
     */
    public OrderDTO updateOrderStatus(String orderId, Boolean payStatus, Boolean pickStatus) {
        try {
            logger.info("更新購物車訂單狀態 - 訂單ID: {}", orderId);

            // 根據 bookingId 查詢訂單
            Optional<Order> orderOptional = orderRepository.findByBookingId(orderId);
            if (!orderOptional.isPresent()) {
                logger.warn("更新購物車訂單狀態 - 訂單不存在 - 訂單ID: {}", orderId);
                return null;
            }

            Order order = orderOptional.get();
            if (payStatus != null) {
                order.setPayStatus(payStatus);
            }
            if (pickStatus != null) {
                order.setPickStatus(pickStatus);
            }

            Order updatedOrder = orderRepository.save(order);
            logger.info("購物車訂單狀態更新成功 - 訂單ID: {}", orderId);
            return convertToOrderDTO(updatedOrder);
        } catch (Exception e) {
            logger.error("更新購物車訂單狀態失敗 - 訂單ID: {} - 錯誤: {}",
                    orderId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 刪除購物車訂單
     */
    public boolean deleteOrder(Integer orderId) {
        try {
            if (orderRepository.existsById(orderId)) {
                orderRepository.deleteById(orderId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("刪除購物車訂單失敗 - 訂單ID: {} - 錯誤: {}",
                    orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根據 booking_id 查詢訂單
     */
    public OrderDTO getOrderByBookingId(String bookingId) {
        try {
            logger.info("根據 booking_id 查詢購物車訂單 - bookingId: {}", bookingId);
            // 需要在 Repository 中新增這個方法
            Optional<Order> orderOptional = orderRepository.findByBookingId(bookingId);
            if (orderOptional.isPresent()) {
                return convertToOrderDTO(orderOptional.get());
            }
            logger.warn("查詢購物車訂單 - 訂單不存在 - bookingId: {}", bookingId);
            return null;
        } catch (Exception e) {
            logger.error("查詢購物車訂單失敗 - bookingId: {} - 錯誤: {}",
                    bookingId, e.getMessage(), e);
            return null;
        }
    }

    public OrderDTO getOrderDetails(Integer orderId) {
        try {
            logger.info("Fetching order details - orderId: {}", orderId);
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                OrderDTO orderDTO = convertToOrderDTO(order);
                return orderDTO;
            } else {
                logger.warn("Order not found - orderId: {}", orderId);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error fetching order details - orderId: {} - error: {}", orderId, e.getMessage(), e);
            return null;
        }
    }
    // ======================工具方法======================//

    /**
     * 將場地訂單實體轉換為DTO
     */
    private BookingVenuesResponseDTO convertToVenueDTO(BookingVenues venue) {
        BookingVenuesResponseDTO dto = new BookingVenuesResponseDTO();
        dto.setBookingVenuesId(venue.getBookingVenuesId());
        dto.setVenuesCategory(venue.getVenuesCategory());
        dto.setVenuesUnitName(venue.getVenuesUnitName());
        dto.setVenuesNo(venue.getVenuesNo());
        dto.setOpenDate(venue.getOpenDate());
        dto.setTimeSlotName(venue.getTimeSlotName());
        dto.setVenuesStatus(venue.getVenuesStatus());
        dto.setVenuesPrice(venue.getVenuesPrice());
        dto.setBookingStatus(venue.getBookingStatus());
        dto.setBookingId(venue.getBookingId());
        dto.setOrderTime(venue.getOrderTime());

        if (venue.getMember() != null) {
            dto.setMember(venue.getMember());
        }
        return dto;
    }

    /**
     * 根據 booking_id 查詢訂單
     */
    public List<OrderDTO> searchOrders(String bookingId) {
        try {
            logger.info("根據訂單編號查詢訂單: {}", bookingId);

            List<Order> orders;
            if (bookingId != null && !bookingId.isEmpty()) {
                // 如果有提供 bookingId，則進行模糊查詢
                orders = orderRepository.findByBookingIdContaining(bookingId);
            } else {
                // 如果沒有提供 bookingId，則返回所有訂單
                orders = orderRepository.findAll();
            }

            // 這裡需要將 List<Order> 轉換為 List<OrderDTO>
            return orders.stream()
                    .map(this::convertToOrderDTO) // 將每個 Order 轉換為 OrderDTO
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("根據訂單編號查詢訂單失敗 - 錯誤: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    // ======================購物車訂單工具方法======================//

    /**
     * 將購物車訂單實體轉換為DTO
     */
    private OrderDTO convertToOrderDTO(Order order) {
        if (order == null) {
            return null;
        }

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setMemberId(order.getMemberId());
        dto.setMemberName(order.getMemberName());
        dto.setPhone(order.getPhone());
        dto.setEmail(order.getEmail());
        dto.setPickupType(order.getPickupType());
        dto.setAddress(order.getAddress());
        dto.setTotal(order.getTotal());
        dto.setPayStatus(order.getPayStatus());
        dto.setPickStatus(order.getPickStatus());
        dto.setCreateTime(order.getCreateTime());
        dto.setUpdateTime(order.getUpdateTime());
        dto.setBookingId(order.getBookingId());

        if (order.getItems() != null) {
            List<com.sportcenter.sportcenter.product.dto.OrderItemDTO> items
                    = order.getItems().stream()
                            .map(item -> new com.sportcenter.sportcenter.product.dto.OrderItemDTO(item))
                            .collect(Collectors.toList());
            dto.setItems(items);
        }

        return dto;
    }

    /**
     * 將購物車訂單項目實體轉換為DTO
     */
    private OrderItemDTO convertToOrderItemDTO(OrderItem item) {
        if (item == null) {
            return null;
        }

        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setNormId(item.getNormId());
        dto.setProductName(item.getProductName());
        dto.setNormName(item.getNormName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());

        return dto;
    }
}
