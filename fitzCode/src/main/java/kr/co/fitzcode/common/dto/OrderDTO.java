package kr.co.fitzcode.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.fitzcode.common.enums.DeliveryStatus;
import kr.co.fitzcode.common.enums.OrderStatus;
import lombok.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@ToString
@Schema(description = "주문 정보")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @Schema(description = "주문번호")
    private int orderId;

    @Schema(description = "사용자 아이디")
    private int userId;

    private int addressId;

    @Schema(description = "총 주문 금액")
    private Integer totalPrice;

    @Schema(description = "주문 상태 코드")
    private int orderStatus;


    @Schema(description = "주문일")
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    @Schema(description = "운송장 번호")
    private String trackingNumber;

    @Schema(description = "배송 상태")
    private int deliveryStatus;

    @Schema(description = "발송 날짜")
    private LocalDateTime shippedAt;

    @Schema(description = "배송 완료 날짜")
    private LocalDateTime deliveredAt;


    // 주문 상태
    public OrderStatus getStatus() {
        return OrderStatus.fromCode(orderStatus);
    }

    // 배송 상태
    public DeliveryStatus getDeliveryStatus() {
        return DeliveryStatus.fromCode(deliveryStatus);
    }

    // 주문날짜 포맷팅
    public String getFormattedCreatedAt() {
        if (createdAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            return createdAt.format(formatter);
        }
        return "";
    }
}