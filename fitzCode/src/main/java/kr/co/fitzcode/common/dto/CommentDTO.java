package kr.co.fitzcode.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private int commentId;         // 댓글 고유 ID
    private int postId;            // 게시물 ID
    private int userId;            // 사용자 ID
    private Integer parentCommentId; // 대댓글을 위한 부모 댓글 ID
    private String content;        // 댓글 내용
}
