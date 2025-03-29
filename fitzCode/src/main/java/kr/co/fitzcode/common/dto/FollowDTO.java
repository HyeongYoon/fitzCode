package kr.co.fitzcode.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowDTO {
    private Integer followId;     // 팔로우 ID
    private Integer followerId;   // 팔로워 사용자 ID
    private Integer followingId;  // 팔로잉 사용자 ID
    private LocalDateTime createdAt;  // 팔로우 생성 날짜
}