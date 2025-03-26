package kr.co.fitzcode.community.service;

import kr.co.fitzcode.common.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CommunityService {
    List<ProductTag> searchProductsByName(String productName);
    int insertPost(PostDTO postDTO, List<Long> productIdList, List<MultipartFile> images) throws IOException;
    Map<String, Object> getPostDetail(int postId);
    List<ProductTag> getProductTagsByPostId(int postId);
    List<Map<String, Object>> getOtherStylesByUserId(int userId, int excludePostId);
    List<PostImageDTO> getPostImagesByPostId(int postId);
    List<Map<String, Object>> getAllPosts(String styleCategory);
    PostDTO getPostById(int id);
    void updatePost(PostDTO postDTO, List<Long> productIdList, List<MultipartFile> images) throws IOException;
    List<PostDTO> findByStyleCategory(String styleCategory);
    void deletePost(int postId);
    PostDTO insertPostLike(PostLikeDTO postLikeDTO);
    PostDTO deletePostLike(PostLikeDTO postLikeDTO);
    int countPostLikes(int postId);
    boolean isLiked(int postId, int userId);
    PostDTO insertPostSave(PostSaveDTO postSaveDTO);
    PostDTO deletePostSave(PostSaveDTO postSaveDTO);
    int countPostSaves(int postId);
    boolean isSaved(int postId, int userId);
    // 팔로우
    void addFollow(int followerId, int followingId);
    void deleteFollow(int followerId, int followingId);
    boolean isFollowing(int followerId, int followingId);
}