package kr.co.fitzcode.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmailMessageDTO {
    private String to;
    private String subject;
    private String message;
}
