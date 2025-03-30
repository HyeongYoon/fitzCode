package kr.co.fitzcode.order.controller;

import kr.co.fitzcode.common.dto.OrderDTO;
import kr.co.fitzcode.common.dto.UserOrderDTO;
import kr.co.fitzcode.common.dto.UserOrderDetailDTO;
import kr.co.fitzcode.common.util.SecurityUtils;
import kr.co.fitzcode.order.service.OrderService;
import kr.co.fitzcode.order.service.UserOrderDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final UserOrderDetailService userOrderDetailService;

    // 주문 상세보기
    @GetMapping("/order/{orderId}")
    public String order(@PathVariable int orderId, Model model) {

        UserOrderDTO orderDTO = orderService.getOrderByOrderId(orderId);
        log.info("orderDTO: {}", orderDTO);
        List<UserOrderDetailDTO> list = userOrderDetailService.getOrderDetailByOrderId(orderId);
        log.info("list: {}", list);
        model.addAttribute("orderDTO", orderDTO);
        model.addAttribute("orderDetailList", list);

        return "order/order";
    }

    // 주문 내역
    @GetMapping("/orders")
    public String orders(Model model) {
        int userId = SecurityUtils.getUserId();
        List<UserOrderDetailDTO> list = userOrderDetailService.getOrderDetailByUserId(userId);
        log.info("list::::: {}", list);

//        // Group by orderId
//        Map<LocalDateTime, List<UserOrderDetailDTO>> groupedOrders = list.stream()
//                .collect(Collectors.groupingBy(UserOrderDetailDTO::getCreatedAt));

//        // 각 그룹(리스트) 내부 정렬
//        groupedOrders.forEach((orderId, orderList) ->
//                orderList.sort(Comparator.comparing(UserOrderDetailDTO::getCreatedAt))
//        );

//        log.info("groupedOrders::::: {}", groupedOrders);

        model.addAttribute("groupedOrders", list);
        return "order/allOrders";
    }


}
