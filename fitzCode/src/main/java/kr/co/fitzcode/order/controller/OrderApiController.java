package kr.co.fitzcode.order.controller;

import kr.co.fitzcode.common.dto.*;
import kr.co.fitzcode.common.service.UserService;
import kr.co.fitzcode.common.util.SecurityUtils;
import kr.co.fitzcode.order.service.*;
import kr.co.fitzcode.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
@Slf4j
public class OrderApiController {

    private final OrderService orderService;
    private final UserOrderDetailService userOrderDetailService;
    private final CouponService couponService;
    private final EmailService emailService;
    private final UserService userService;
    private final RefundService refundService;
    private final PaymentService paymentService;

    @GetMapping("/getUserAddress")
    public ResponseEntity<List<AddressDTO>> order() {
        int userId = SecurityUtils.getUserId();
        List<AddressDTO> addressList = orderService.getUserAddress(userId);
        log.info("addressList: {}", addressList);
        return ResponseEntity.ok(addressList);
    }

    @GetMapping("/getUserAddressByAddressId")
    public ResponseEntity<AddressDTO> getUserAddressByAddressId(@RequestParam("addressId") int addressId) {
        AddressDTO address = orderService.getUserAddressByAddressId(addressId);
        return ResponseEntity.ok(address);
    }

    @PostMapping("/createOrderFromDetailPage")
    public ResponseEntity<OrderDTO> order(@RequestParam("productId") int productId,
                                          @RequestParam("addressLine1") String addressLine1,
                                          @RequestParam("postalCode") String postalCode,
                                          @RequestParam("sizeCode") int sizeCode,
                                          @RequestParam("price") int price,
                                          @RequestParam(value = "couponId", required = false) Integer couponId,
                                          @RequestParam("impUid") String impUid
                                          ) {

        int userId = SecurityUtils.getUserId();
        addAddressForUserIfNotExists(userId ,addressLine1, postalCode);


        int addressId = orderService.getAddressIdUsingAddressLine1AndPostalCode(addressLine1, postalCode, userId);
        log.info("addressId::::::::::::::: {}", addressId);

        // create order now
        OrderDTO orderDTO = OrderDTO.builder()
                .userId(userId)
                .addressId(addressId)
                .totalPrice(price)
                .orderStatus(1)
                .deliveryStatus(1)
                .build();

        int orderId = orderService.insertNewOrder(orderDTO);
        log.info("orderId just made::::::::::::::: {}", orderId);

        log.info("impUid::::::::::::::::::: {}", impUid);
        addImpUid(impUid, price, orderId);


        // create a product map
        List<Map<String, Object>> batchInsertList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderId", orderId);
        paramMap.put("productId", productId);
        paramMap.put("sizeCode", sizeCode);
        paramMap.put("price", price);
        paramMap.put("quantity", 1);
        paramMap.put("couponId", couponId);
        batchInsertList.add(paramMap);

        // mark coupon as used
        couponService.markCouponAsUsed(userId, couponId, orderId);

        // add order details
        userOrderDetailService.addOrderDetailToOrder(batchInsertList);

        // decrease product amount by 1
        userOrderDetailService.decrementProductCount(productId, sizeCode);

        // send email
        String email = userService.getUserEmailByUserId(userId);
        List<UserOrderDetailDTO> list = userOrderDetailService.getOrderDetailByOrderId(orderId);
        log.info("email::::::::::::::::: {}", email);
        EmailMessageDTO emailMessageDTO = EmailMessageDTO.builder()
                .to(email)
                .subject("Purchase complete")
                .build();
        String send = emailService.sendOrderConfirmationEmail(emailMessageDTO, orderDTO, list);
        log.info("send::::::::::::::::::: {}", send);
        log.info(orderDTO.toString());
        return ResponseEntity.ok(orderDTO);
    }



    @PostMapping("/createOrderFromCart")
    public ResponseEntity<?> createOrderFromCart(@RequestBody Map<String, Object> orderData) {
        try {
            // Extract payment details
            String impUid = (String) orderData.get("imp_uid");
            String merchantUid = (String) orderData.get("merchant_uid");
            Integer totalPrice = (Integer) orderData.get("totalPrice");

            // Extract order details
            Integer addressId = (Integer) orderData.get("addressId");
            String addressLine1 = (String) orderData.get("addressLine1");
            String postalCode = (String) orderData.get("postalCode");

            Object orderDetailObj = orderData.get("orderDetail");

            List<Map<String, Object>> orderDetail;
            if (orderDetailObj instanceof List) {
                orderDetail = (List<Map<String, Object>>) orderDetailObj;
            } else {
                throw new IllegalArgumentException("Invalid orderDetail format");
            }

            log.info("impUid::::::::::::::: {}", impUid);
            log.info("merchantUid::::::::::::::: {}", merchantUid);
            log.info("addressId::::::::::::::: {}", addressId);
            log.info("addressLine1::::::::::::: {}", addressLine1);
            log.info("postalCode::::::::::::::: {}", postalCode);
            log.info("orderDetail::::::::::::::: {}", orderDetail);

            int userId = SecurityUtils.getUserId();
            addAddressForUserIfNotExists(userId ,addressLine1, postalCode);


            int newAddressId = orderService.getAddressIdUsingAddressLine1AndPostalCode(addressLine1, postalCode, userId);
            log.info("addressId::::::::::::::: {}", newAddressId);



            // create order now
            OrderDTO orderDTO = OrderDTO.builder()
                    .userId(userId)
                    .addressId(newAddressId)
                    .totalPrice(totalPrice)
                    .orderStatus(1)
                    .deliveryStatus(1)
                    .build();

            int orderId = orderService.insertNewOrder(orderDTO);
            log.info("orderId just made::::::::::::::: {}", orderId);

            // TODO: add impUid to payment table
            addImpUid(impUid, totalPrice, orderId);


            List<Map<String, Object>> batchInsertList = new ArrayList<>();

            for (Map<String, Object> stringObjectMap : orderDetail) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("orderId", orderId);
                paramMap.put("productId", stringObjectMap.get("productId"));
                paramMap.put("sizeCode", stringObjectMap.get("sizeCode"));
                paramMap.put("price", stringObjectMap.get("finalPrice"));
                paramMap.put("quantity", stringObjectMap.get("quantity"));
                paramMap.put("couponId", stringObjectMap.get("couponId"));
                couponService.markCouponAsUsed(userId, (Integer) stringObjectMap.get("couponId"), orderId);
                batchInsertList.add(paramMap);

                // decrease product amount by 1
                userOrderDetailService.decrementProductCount((Integer) stringObjectMap.get("productId"), (Integer) stringObjectMap.get("sizeCode"));
            }
            log.info("batchInsertList::::::::::::::: {}", batchInsertList);
            userOrderDetailService.addOrderDetailToOrder(batchInsertList);



            //send email
            String email = userService.getUserEmailByUserId(userId);
            List<UserOrderDetailDTO> list = userOrderDetailService.getOrderDetailByOrderId(orderId);
            log.info("email::::::::::::::::: {}", email);
            EmailMessageDTO emailMessageDTO = EmailMessageDTO.builder()
                    .to(email)
                    .subject("Purchase complete")
                    .build();
            String send = emailService.sendOrderConfirmationEmail(emailMessageDTO, orderDTO, list);
            log.info("send::::::::::::::::::: {}", send);



            return ResponseEntity.ok(orderDTO);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing order: " + e.getMessage());
        }
    }

    private void addImpUid(String impUid, int totalPrice, int orderId) {
        PaymentDTO paymentDTO = PaymentDTO.builder()
                .transactionId(impUid)
                .paymentMethod(1)
                .paymentStatus(1)
                .amount(totalPrice)
                .orderId(orderId)
                .build();
        orderService.addPayment(paymentDTO);
    }

    private void addAddressForUserIfNotExists(int userId, String addressLine1, String postalCode) {
        if (orderService.checkIfAddressExistsForUser(userId ,addressLine1, postalCode)) {
            log.info("address already exists for user");
        } else {
            log.info("address does not exists for user, creating address information");
            AddressDTO addressDTO = AddressDTO.builder()
                    .addressLine1(addressLine1)
                    .postalCode(postalCode)
                    .build();
            orderService.addNonDefaultAddressForUser(userId, addressDTO);
        }
    }




    @PostMapping("/refund")
    public ResponseEntity<String> orderRefund(@RequestParam("orderDetailId") int orderDetailId) {
        log.info("orderDetailId::::: {}", orderDetailId);
        userOrderDetailService.updateRequestRefundStatus(orderDetailId);
        refundService.requestRefund(orderDetailId);
        return ResponseEntity.ok("Order refunded successfully");
    }
}
