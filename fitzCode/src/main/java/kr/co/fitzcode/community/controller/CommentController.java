package kr.co.fitzcode.community.controller;

import jakarta.servlet.http.HttpSession;
import kr.co.fitzcode.common.dto.CommentDTO;
import kr.co.fitzcode.common.dto.UserDTO;
import kr.co.fitzcode.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // 댓글 또는 대댓글 추가
    @PostMapping("add/{postId}")
    public Map<String, Object> addComment(
            @PathVariable("postId") int postId,
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        if (userDTO == null) {
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        String content = (String) requestBody.get("content");
        Integer parentCommentId = requestBody.get("parentCommentId") != null ?
                Integer.parseInt(requestBody.get("parentCommentId").toString()) : null;

        CommentDTO commentDTO = commentService.addComment(postId, userDTO.getUserId(), content, parentCommentId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comment", commentDTO);
        response.put("commentCount", commentService.countPostComments(postId));
        response.put("userName", userDTO.getUserName());
        response.put("profileImage", userDTO.getProfileImage());
        return response;
    }

    // 게시물 댓글 조회
    @GetMapping("get/{postId}")
    public List<Map<String, Object>> getComments(@PathVariable("postId") int postId, HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        int currentUserId = userDTO.getUserId();

        List<Map<String, Object>> rawComments = commentService.getCommentsByPostId(postId);
        System.out.println("Fetching comments for postId: " + postId + ", currentUserId: " + currentUserId);
        rawComments.forEach(comment -> System.out.println("Comment: " + comment));
        return rawComments.stream().map(rawComment -> {
            Map<String, Object> mappedComment = new HashMap<>();
            mappedComment.put("commentId", rawComment.get("comment_id"));
            mappedComment.put("postId", rawComment.get("post_id"));
            mappedComment.put("userId", rawComment.get("user_id"));
            mappedComment.put("parentCommentId", rawComment.get("parent_comment_id"));
            mappedComment.put("content", rawComment.get("content"));
            mappedComment.put("userName", rawComment.get("user_name"));
            mappedComment.put("profileImage", rawComment.get("profile_image"));
            mappedComment.put("likeCount", rawComment.get("like_count"));
            mappedComment.put("isLiked", currentUserId != -1 && commentService.isCommentLikedByUser(
                    (Integer) rawComment.get("comment_id"), currentUserId));
            return mappedComment;
        }).collect(Collectors.toList());
    }

    // 댓글 삭제
    @DeleteMapping("delete/{commentId}")
    public Map<String, Object> deleteComment(
            @PathVariable("commentId") int commentId,
            HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        if (userDTO == null) {
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        try {
            commentService.deleteComment(commentId, userDTO.getUserId());
            return Map.of("success", true);
        } catch (IllegalStateException e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    // 댓글 수정
    @PutMapping("modify/{commentId}")
    public Map<String, Object> updateComment(
            @PathVariable("commentId") int commentId,
            @RequestBody Map<String, String> requestBody,
            HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        if (userDTO == null) {
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        String content = requestBody.get("content");
        try {
            CommentDTO updatedComment = commentService.updateComment(commentId, userDTO.getUserId(), content);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("comment", updatedComment);
            return response;
        } catch (IllegalStateException e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    // 댓글 좋아요 추가
    @PostMapping("like/{commentId}")
    public Map<String, Object> likeComment(@PathVariable("commentId") int commentId, HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        if (userDTO == null) {
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        try {
            System.out.println("Liking commentId: " + commentId + ", userId: " + userDTO.getUserId());
            commentService.addCommentLike(commentId, userDTO.getUserId());
            int likeCount = commentService.countCommentLikes(commentId);
            return Map.of("success", true, "likeCount", likeCount);
        } catch (IllegalArgumentException e) {
            return Map.of("success", false, "message", e.getMessage());
        } catch (IllegalStateException e) {
            return Map.of("success", false, "message", e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            return Map.of("success", false, "message", "좋아요 처리 중 오류 발생: " + e.getMessage());
        }
    }

    // 댓글 좋아요 삭제
    @DeleteMapping("unlike/{commentId}")
    public Map<String, Object> unlikeComment(@PathVariable("commentId") int commentId, HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        if (userDTO == null) {
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        commentService.deleteCommentLike(commentId, userDTO.getUserId());
        int likeCount = commentService.countCommentLikes(commentId);
        return Map.of("success", true, "likeCount", likeCount);
    }
}