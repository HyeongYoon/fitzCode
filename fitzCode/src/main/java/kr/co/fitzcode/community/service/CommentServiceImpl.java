package kr.co.fitzcode.community.service;

import kr.co.fitzcode.common.dto.CommentDTO;
import kr.co.fitzcode.common.dto.CommentLikeDTO;
import kr.co.fitzcode.common.dto.UserDTO;
import kr.co.fitzcode.community.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    @Override
    public CommentDTO addComment(int postId, int userId, String content, Integer parentCommentId) {
        CommentDTO commentDTO = CommentDTO.builder()
                .postId(postId)
                .userId(userId)
                .parentCommentId(parentCommentId)
                .content(content)
                .build();

        commentMapper.insertComment(commentDTO);

        UserDTO user = commentMapper.getUserById(userId);
        user.setUserName(user.getUserName());
        user.setProfileImage(user.getProfileImage());

        int commentCount = commentMapper.countPostComments(postId);
        Map<String, Object> params = new HashMap<>();
        params.put("postId", postId);
        params.put("commentCount", commentCount);
        commentMapper.updateCommentCount(params);

        return commentDTO;
    }

    @Override
    public int countPostComments(int postId) {
        return commentMapper.countPostComments(postId);
    }

    @Override
    public List<Map<String, Object>> getCommentsByPostId(int postId) {
        return commentMapper.getCommentsByPostId(postId);
    }

    @Override
    public void deleteComment(int commentId, int userId) {
        CommentDTO comment = commentMapper.getCommentById(commentId);
        if (comment != null && comment.getUserId() == userId) {
            commentMapper.deleteComment(commentId);
            int commentCount = commentMapper.countPostComments(comment.getPostId());
            Map<String, Object> params = new HashMap<>();
            params.put("postId", comment.getPostId());
            params.put("commentCount", commentCount);
            commentMapper.updateCommentCount(params);
        } else {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }
    }

    @Override
    public CommentDTO updateComment(int commentId, int userId, String content) {
        CommentDTO comment = commentMapper.getCommentById(commentId);
        if (comment != null && comment.getUserId() == userId) {
            comment.setContent(content);
            commentMapper.updateComment(comment);
            return comment;
        } else {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }
    }

    @Override
    public void addCommentLike(int commentId, int userId) {
        CommentLikeDTO commentLikeDTO = CommentLikeDTO.builder()
                .commentId(commentId)
                .userId(userId)
                .build();
        commentMapper.addCommentLike(commentLikeDTO);
    }

    @Override
    public void deleteCommentLike(int commentId, int userId) {
        CommentLikeDTO commentLikeDTO = CommentLikeDTO.builder()
                .commentId(commentId)
                .userId(userId)
                .build();
        commentMapper.deleteCommentLike(commentLikeDTO);
    }

    @Override
    public int countCommentLikes(int commentId) {
        return commentMapper.countCommentLikes(commentId);
    }

    @Override
    public boolean isCommentLikedByUser(int commentId, int userId) {
        CommentLikeDTO commentLikeDTO = CommentLikeDTO.builder()
                .commentId(commentId)
                .userId(userId)
                .build();
        return commentMapper.isCommentLikedByUser(commentLikeDTO);
    }
}