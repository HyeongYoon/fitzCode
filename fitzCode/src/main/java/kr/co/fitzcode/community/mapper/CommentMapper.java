package kr.co.fitzcode.community.mapper;

import kr.co.fitzcode.common.dto.CommentDTO;
import kr.co.fitzcode.common.dto.CommentLikeDTO;
import kr.co.fitzcode.common.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper {
    void insertComment(CommentDTO commentDTO);
    int countPostComments(int postId);
    void updateCommentCount(Map<String, Object> params);
    UserDTO getUserById(int userId);
    List<Map<String, Object>> getCommentsByPostId(int postId);
    CommentDTO getCommentById(int commentId);
    void deleteComment(int commentId);
    void updateComment(CommentDTO commentDTO);
    // 댓글 좋아요
    void addCommentLike(CommentLikeDTO commentLikeDTO);
    void deleteCommentLike(CommentLikeDTO commentLikeDTO);
    int countCommentLikes(int commentId);
    boolean isCommentLikedByUser(CommentLikeDTO commentLikeDTO);
}
