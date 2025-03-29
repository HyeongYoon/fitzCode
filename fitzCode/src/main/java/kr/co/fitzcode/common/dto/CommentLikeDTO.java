package kr.co.fitzcode.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeDTO {
    private int likeId;         // 좋아요 고유 ID
    private int commentId;      // 댓글 ID
    private int userId;         // 사용자 ID
}