package kr.co.fitzcode.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryCategory {
    GENERAL(1, "일반문의"),
    PRODUCT(2, "상품문의"),
    REFUND(3, "환불문의"),
    PAYMENT(4, "결제문의"),
    RETURN(5, "반품문의");

    private final int code;
    private final String description;

    public static InquiryCategory fromCode(int code) {
        for (InquiryCategory category : values()) {
            if (category.getCode() == code) {
                return category;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 문의 카테고리 코드: " + code);
    }
}