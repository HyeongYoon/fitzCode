package kr.co.fitzcode.community.mapper;

import kr.co.fitzcode.common.dto.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommunityMapper {
    List<ProductTag> searchProductsByName(String productName);
    void insertPost(PostDTO postDTO);
    void insertPostImage(PostImageDTO postImageDTO);
    void insertPostTags(Map<String, Object> map);
    Map<String, Object> getPostDetail(int postId);
    List<ProductTag> getProductTagsByPostId(int postId);
    List<Map<String, Object>> getOtherStylesByUserId(Map<String, Object> params);
    List<PostImageDTO> getPostImagesByPostId(int postId);
    List<Map<String, Object>> getAllPosts(String styleCategory);
    PostDTO getPostById(int id);
    void updatePost(PostDTO postDTO, List<Long> productIdList, List<String> imageUrls);
    List<PostDTO> findByStyleCategory(String styleCategory);
    void deletePost(int postId);
    // 좋아요
    void insertPostLike(PostLikeDTO postLikeDTO);
    void deletePostLike(PostLikeDTO postLikeDTO);
    int countPostLikes(int postId);
    boolean existsPostLike(PostLikeDTO postLikeDTO);
    void updateLikeCount(Map<String, Object> params);
    // 북마크
    void insertPostSave(PostSaveDTO postSaveDTO);
    void deletePostSave(PostSaveDTO postSaveDTO);
    int countPostSaves(int postId);
    boolean existsPostSave(PostSaveDTO postSaveDTO);
    void updateSaveCount(Map<String, Object> params);
    // 팔로우
    void addFollow(FollowDTO followDTO);
    void deleteFollow(FollowDTO followDTO);
    boolean isFollowing(FollowDTO followDTO);
    // 좋아요 수 기준 상위 게시물 조회
    List<Map<String, Object>> getTopLikedPosts(int limit);
}