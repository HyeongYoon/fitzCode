package kr.co.fitzcode.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private int postId; // 게시물 고유 ID
    private int userId; // 작성자 ID
    private String styleCategory; // 스타일 카테고리 ID
    private String title; // 게시물 제목
    private String content; // 게시물 내용
    private String thumbnailImageUrl; // 썸네일 이미지 URL
    private int likeCount; // 좋아요 수
    private int commentCount; // 댓글 수
    private int viewCount; // 조회수
    private int saveCount; // 저장 수
    private LocalDateTime createdAt; // 생성 날짜
    private LocalDateTime updatedAt; // 수정 날짜
}