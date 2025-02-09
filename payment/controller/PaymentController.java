package com.sportcenter.sportcenter.payment.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sportcenter.sportcenter.payment.dto.BookingVenuesRequestDTO;
import com.sportcenter.sportcenter.payment.entity.Payment;
import com.sportcenter.sportcenter.payment.repository.PaymentRepository;
import com.sportcenter.sportcenter.payment.service.OrderService;
import com.sportcenter.sportcenter.payment.service.PaymentService;

@Controller
@RequestMapping("/payment")
@CrossOrigin
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentRepository paymentRepository;

    @PostMapping("/ecpay/create/{bookingId}")
    @ResponseBody
    public ResponseEntity<?> createEcpayPayment(@PathVariable String bookingId) {
        logger.info("開始建立綠界支付 - 訂單編號: {}", bookingId);

        try {
            String paymentForm = paymentService.createEcpayPayment(bookingId);
            logger.info("綠界支付表單生成成功 - 訂單編號: {}", bookingId);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "form", paymentForm));
        } catch (Exception e) {
            logger.error("建立綠界支付失敗 - 訂單編號: {} - 錯誤: {}", bookingId, e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/ecpay/callback")
    @ResponseBody
    public String handleEcpayCallback(@RequestParam Map<String, String> callbackData) {
        logger.info("收到綠界支付回調 - 回調資料: {}", callbackData);

        try {
            String rtnCode = callbackData.get("RtnCode");
            String rtnMsg = callbackData.get("RtnMsg");
            String merchantTradeNo = callbackData.get("MerchantTradeNo");
            Payment payment = paymentRepository.findByMerchantTradeNo(merchantTradeNo);
            String orderId = payment.getOrderId();

            logger.info("處理支付回調 - 訂單編號: {}, 回傳代碼: {}, 回傳訊息: {}",
                    orderId, rtnCode, rtnMsg);

            if ("1".equals(rtnCode)) {
                boolean updateSuccess = false;

                if (orderId.startsWith("VE")) {
                    // 更新場地訂單狀態
                    BookingVenuesRequestDTO requestDTO = new BookingVenuesRequestDTO();
                    requestDTO.setBookingStatus("已付款");
                    updateSuccess = orderService.updateVenueBooking(orderId, requestDTO) != null;
                } else if (orderId.startsWith("PR")) {
                    // 更新購物車訂單狀態
                    // 直接使用 orderId，假設 orderId 本身已經是訂單的完整編號
                    updateSuccess = orderService.updateOrderStatus(orderId, true, null) != null;

                } else {
                    // 更新課程訂單狀態
                    String requestJson = String.format(
                            "{\"bookingId\":\"%s\",\"bookingStatus\":\"已付款\"}",
                            orderId);
                    updateSuccess = orderService.modify(requestJson) != null;
                }

                if (updateSuccess) {
                    // 更新支付記錄
                    paymentService.updatePaymentStatus(orderId, "已付款", merchantTradeNo);
                    logger.info("訂單狀態和支付記錄更新成功 - 訂單編號: {}", orderId);
                    return "1|OK";
                } else {
                    logger.error("訂單狀態更新失敗 - 訂單編號: {}", orderId);
                    return "0|OrderUpdateFailed";
                }
            } else {
                // 支付失敗，更新支付記錄為失敗
                paymentService.updatePaymentStatus(orderId, "付款失敗", merchantTradeNo);
                logger.warn("支付未成功 - 訂單編號: {}, 回傳代碼: {}", orderId, rtnCode);
                return "0|" + rtnMsg;
            }
        } catch (Exception e) {
            logger.error("處理支付回調時發生錯誤: {}", e.getMessage(), e);
            return "0|Error";
        }
    }

    @GetMapping("/ecpay/status/{bookingId}")
    @ResponseBody
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String bookingId) {
        logger.info("查詢支付狀態 - 訂單編號: {}", bookingId);

        try {
            String status;
            if (bookingId.startsWith("VE")) {
                // 查詢場地訂單狀態
                var venueBooking = orderService.findVenueByBookingId(bookingId);
                status = venueBooking != null ? venueBooking.getBookingStatus() : null;
            } else {
                // 查詢課程訂單狀態
                var courseOrder = orderService.findByBookingId(bookingId);
                status = courseOrder != null ? courseOrder.getBookingStatus() : null;
            }

            if (status != null) {
                logger.info("支付狀態查詢成功 - 訂單編號: {}, 狀態: {}", bookingId, status);
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "status", status));
            }

            logger.warn("查詢支付狀態失敗 - 找不到訂單: {}", bookingId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("查詢支付狀態時發生錯誤 - 訂單編號: {} - 錯誤: {}", bookingId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }
}