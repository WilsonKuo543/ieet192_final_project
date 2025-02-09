package com.sportcenter.sportcenter.payment.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportcenter.sportcenter.course.entity.CourseOrder;
import com.sportcenter.sportcenter.payment.dto.BookingVenuesResponseDTO;
import com.sportcenter.sportcenter.payment.dto.OrderDTO;
import com.sportcenter.sportcenter.payment.dto.OrderItemDTO;
import com.sportcenter.sportcenter.payment.entity.Payment;
import com.sportcenter.sportcenter.payment.repository.PaymentRepository;

import ecpay.payment.integration.AllInOne;
import ecpay.payment.integration.domain.AioCheckOutALL;


@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final String RETURN_URL = "https://3004-1-160-24-87.ngrok-free.app/api/payment/ecpay/callback";

    private static final String CLIENT_BACK_URL = "http://192.168.33.96:5173/test-payment";
    private static final String PRODUCT_URL = "http://192.168.33.96:5173/products-test-payment";
    private static final String VENUE_URL = "http://192.168.33.96:5173/venues-test-payment";


    @Transactional
    public String createEcpayPayment(String bookingId) {
        logger.info("開始建立綠界支付 - 訂單編號: {}", bookingId);

        try {
            int totalAmount;
            String currentStatus;
            String orderDescription;
            String itemName;

            if (bookingId.startsWith("VE")) {
                // 場地訂單
                BookingVenuesResponseDTO venueOrder = orderService.findVenueByBookingId(bookingId);
                if (venueOrder == null) {
                    logger.error("找不到場地訂單 - 訂單編號: {}", bookingId);
                    throw new RuntimeException("找不到訂單");
                }
                totalAmount = venueOrder.getVenuesPrice();
                currentStatus = venueOrder.getBookingStatus();
                orderDescription = "場地訂單";

                itemName = String.format("%s-%s (場地租借)",
                        venueOrder.getVenuesCategory(),
                        venueOrder.getVenuesUnitName());
            } else if (bookingId.startsWith("PR")) {
                // 購物車訂單
                OrderDTO cartOrder = orderService.getOrderByBookingId(bookingId);
                if (cartOrder == null) {
                    logger.error("找不到購物車訂單 - 訂單編號: {}", bookingId);
                    throw new RuntimeException("找不到訂單");
                }

                StringBuilder itemDetails = new StringBuilder();
                // 將 product.dto.OrderItemDTO 轉換為 payment.dto.OrderItemDTO
                List<OrderItemDTO> items = cartOrder.getItems().stream()
                    .map(productItem -> {
                        OrderItemDTO paymentItem = new OrderItemDTO();
                        paymentItem.setId(productItem.getId());
                        paymentItem.setProductId(productItem.getProductId());
                        paymentItem.setProductName(productItem.getProductName());
                        paymentItem.setPrice(productItem.getPrice());
                        paymentItem.setQuantity(productItem.getQuantity());
                        paymentItem.setNormId(productItem.getNormId());
                        paymentItem.setNormName(productItem.getNormName());
                        paymentItem.setSubtotal(productItem.getSubtotal());
                        return paymentItem;
                    })
                    .collect(Collectors.toList());

                for (OrderItemDTO item : items) {
                    itemDetails.append(String.format("%s %d元 X %d#",
                            item.getProductName(),
                            item.getPrice(),
                            item.getQuantity()));
                }

                totalAmount = cartOrder.getTotal();
                currentStatus = cartOrder.getPayStatus() ? "已付款" : "待付款";
                orderDescription = "購物車訂單";
                itemName = itemDetails.toString();
            } else {
                
                // 課程訂單
                CourseOrder courseOrder = orderService.findByBookingId(bookingId);
                if (courseOrder == null) {
                    logger.error("找不到課程訂單 - 訂單編號: {}", bookingId);
                    throw new RuntimeException("找不到訂單");
                }
                totalAmount = courseOrder.getTotalPrice();
                currentStatus = courseOrder.getBookingStatus();
                orderDescription = "課程訂單";
                
                // 使用 getCourse().getCourseId() 取得課程ID
                String courseName = orderService.getCourseName(courseOrder.getCourse().getCourseId());
                itemName = String.format("%s (課程訂購)", courseName);
            }

            // 檢查訂單狀態
            if (!"待付款".equals(currentStatus)) {
                logger.error("訂單狀態不正確 - 訂單編號: {}", bookingId);
                throw new RuntimeException("訂單狀態不正確");
            }

            // 查找或創建支付記錄
            Payment payment;
            JSONObject query = new JSONObject();
            query.put("orderId", bookingId);
            List<Payment> existingPayments = paymentRepository.find(query);

            if (existingPayments != null && !existingPayments.isEmpty()) {
                payment = existingPayments.get(0);
                logger.info("使用現有支付記錄 - 訂單編號: {}", bookingId);
            } else {
                payment = new Payment();
                payment.setOrderId(bookingId);
                payment.setAmount(totalAmount);
                payment.setPaymentType("ECPay");
                payment.setPaymentStatus("待付款");
                paymentRepository.insert(payment);
                logger.info("創建新支付記錄 - 訂單編號: {}", bookingId);
            }

            // 生成新的綠界專用訂單編號
            String ecpayTradeNo = generateEcpayTradeNo(bookingId);
            payment.setMerchantTradeNo(ecpayTradeNo);
            paymentRepository.update(payment);
            logger.info("更新支付記錄綠界訂單編號 - 訂單編號: {}, 綠界訂單編號: {}", bookingId, ecpayTradeNo);

            // 建立綠界支付物件
            AllInOne all = new AllInOne("");
            AioCheckOutALL obj = new AioCheckOutALL();

            // 設置必要的參數
            obj.setMerchantTradeNo(ecpayTradeNo);
            obj.setMerchantTradeDate(getCurrentTime());
            obj.setTotalAmount(String.valueOf(totalAmount));
            obj.setTradeDesc(orderDescription);
            obj.setItemName(itemName);
            obj.setReturnURL(RETURN_URL);
            if (bookingId.startsWith("PR")) {
                obj.setClientBackURL(PRODUCT_URL);
            } else if (bookingId.startsWith("VE")) {
                obj.setClientBackURL(VENUE_URL);
            } else {
                obj.setClientBackURL(CLIENT_BACK_URL);
            }
            obj.setNeedExtraPaidInfo("N");

            // 產生支付表單
            String form = all.aioCheckOut(obj, null);
            logger.info("綠界支付表單生成成功 - 訂單編號: {}, 綠界訂單編號: {}", bookingId, ecpayTradeNo);
            return form;

        } catch (Exception e) {
            logger.error("建立綠界支付失敗 - 訂單編號: {} - 錯誤: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("建立支付失敗: " + e.getMessage());
        }
    }

    @Transactional
    public void handlePaymentCallback(String bookingId, String status, String merchantTradeNo) {
        logger.info("處理支付回調 - 訂單編號: {}, 綠界訂單編號: {}, 狀態: {}", bookingId, merchantTradeNo, status);
        try {
            updatePaymentStatus(bookingId, status, merchantTradeNo);

            // 如果是購物車訂單，需要更新訂單的支付狀態
            if (bookingId.startsWith("PR")) {
                OrderDTO order = orderService.getOrderByBookingId(bookingId);
                if (order != null) {
                    boolean payStatus = "已付款".equals(status);
                    orderService.updateOrderStatus(order.getBookingId(), payStatus, null);
                    logger.info("更新購物車訂單支付狀態成功 - 訂單編號: {}", bookingId);
                }
            }
        } catch (Exception e) {
            logger.error("處理支付回調失敗 - 訂單編號: {}, 綠界訂單編號: {} - 錯誤: {}",
                    bookingId, merchantTradeNo, e.getMessage(), e);
            throw new RuntimeException("處理支付回調失敗: " + e.getMessage());
        }
    }

    public Payment getPaymentByOrderId(String bookingId) {
        try {
            JSONObject query = new JSONObject();
            query.put("orderId", bookingId);
            List<Payment> payments = paymentRepository.find(query);
            return payments != null && !payments.isEmpty() ? payments.get(0) : null;
        } catch (Exception e) {
            logger.error("查詢支付記錄失敗 - 訂單編號: {} - 錯誤: {}", bookingId, e.getMessage());
            return null;
        }
    }

    @Transactional
    public void updatePaymentStatus(String bookingId, String status, String merchantTradeNo) {
        logger.info("更新支付記錄 - 訂單編號: {}, 狀態: {}, 綠界訂單編號: {}", bookingId, status, merchantTradeNo);
        try {
            JSONObject query = new JSONObject();
            query.put("merchantTradeNo", merchantTradeNo);
            List<Payment> payments = paymentRepository.find(query);

            if (payments != null && !payments.isEmpty()) {
                Payment payment = payments.get(0);
                payment.setPaymentStatus(status);
                if (!"已取消".equals(status)) {
                    payment.setPaymentTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                }
                paymentRepository.save(payment);
                logger.info("支付記錄更新成功 - 訂單編號: {}, 綠界訂單編號: {}", bookingId, merchantTradeNo);
            } else {
                logger.error("找不到支付記錄 - 訂單編號: {}, 綠界訂單編號: {}", bookingId, merchantTradeNo);
            }
        } catch (Exception e) {
            logger.error("更新支付記錄失敗 - 訂單編號: {}, 綠界訂單編號: {} - 錯誤: {}",
                    bookingId, merchantTradeNo, e.getMessage(), e);
            throw new RuntimeException("更新支付記錄失敗", e);
        }
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    }

    private String generateEcpayTradeNo(String bookingId) {
        // 綠界要求: 訂單編號限制長度為20碼，只接受英數字
        // 格式: 前13碼是原始訂單編號(不夠補0)，後7碼是時間戳記
        String timeStamp = String.valueOf(System.currentTimeMillis()).substring(6);
        String paddedOrderId = String.format("%-13s", bookingId).replace(' ', '0');

        if (paddedOrderId.length() > 13) {
            paddedOrderId = paddedOrderId.substring(0, 13);
        }

        return paddedOrderId + timeStamp;
    }

}