package kr.co.fitzcode.community.service;

import kr.co.fitzcode.common.dto.CommentDTO;

import java.util.List;
import java.util.Map;

public interface CommentService {
    CommentDTO addComment(int postId, int userId, String content, Integer parentCommentId);
    int countPostComments(int postId);
    List<Map<String, Object>> getCommentsByPostId(int postId);
    void deleteComment(int commentId, int userId);
    CommentDTO updateComment(int commentId, int userId, String content);
    void addCommentLike(int commentId, int userId);
    void deleteCommentLike(int commentId, int userId);
    int countCommentLikes(int commentId);
    boolean isCommentLikedByUser(int commentId, int userId);
}