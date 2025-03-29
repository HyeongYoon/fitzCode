package kr.co.fitzcode.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeDTO {
    private int likeId;         // 좋아요 고유 ID
    private int postId;         // 게시물 ID
    private int userId;         // 사용자 ID
}