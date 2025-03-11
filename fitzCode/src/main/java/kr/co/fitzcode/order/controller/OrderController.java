package kr.co.fitzcode.order.controller;

import kr.co.fitzcode.common.dto.AddressDTO;
import kr.co.fitzcode.common.dto.OrderDTO;
import kr.co.fitzcode.common.util.SecurityUtils;
import kr.co.fitzcode.order.service.OrderService;
import kr.co.fitzcode.order.service.UserOrderDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final UserOrderDetailService userOrderDetailService;

    @GetMapping("/getUserAddress")
    public ResponseEntity<List<AddressDTO>> order() {
        int userId = SecurityUtils.getUserId();
        List<AddressDTO> addressList = orderService.getUserAddress(userId);
        return ResponseEntity.ok(addressList);
    }

    @PostMapping("/createOrderFromDetailPage")
    public ResponseEntity<OrderDTO> order(@RequestParam("productId") int productId,
                                          @RequestParam("addressLine1") String addressLine1,
                                          @RequestParam("postalCode") String postalCode,
                                          @RequestParam("sizeCode") int sizeCode,
                                          @RequestParam("price") int price
                                          ) {

        int userId = SecurityUtils.getUserId();

        // create address for user if address is not registered in the db
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
        int addressId = orderService.getAddressIdUsingAddressLine1AndPostalCode(addressLine1, postalCode, userId);
        log.info("addressId::::::::::::::: {}", addressId);

        // create order now
        OrderDTO orderDTO = OrderDTO.builder()
                .userId(userId)
                .addressId(addressId)
                .totalPrice(price)
                .orderStatus(1)
                .build();

        int orderId = orderService.insertNewOrder(orderDTO);
//        log.info("orderId just made::::::::::::::: {}", orderId);

        List<Integer> productIdList = List.of(productId);
        log.info("productIdList::::::::::::::: {}", productIdList);
        userOrderDetailService.addOrderSingleDetailToOrder(orderId, productIdList);




        return null;
    }
}
