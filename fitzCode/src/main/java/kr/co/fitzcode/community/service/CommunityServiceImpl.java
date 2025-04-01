package kr.co.fitzcode.community.service;

import kr.co.fitzcode.common.dto.*;
import kr.co.fitzcode.common.service.S3Service;
import kr.co.fitzcode.community.mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityServiceImpl implements CommunityService {

    private final CommunityMapper communityMapper;
    private final S3Service s3Service;

    @Override
    public List<ProductTag> searchProductsByName(String productName) {
        List<ProductTag> products = communityMapper.searchProductsByName(productName);
        return products != null ? products : Collections.emptyList();
    }

    @Override
    public int insertPost(PostDTO postDTO, List<Long> productIdList, List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            if (images.size() == 1 && !images.get(0).isEmpty()) {
                String imageUrl = s3Service.uploadFile(images.get(0), "community");
                imageUrls.add(imageUrl);
            } else {
                imageUrls = s3Service.uploadFiles(images, "community");
            }
            postDTO.setThumbnailImageUrl(imageUrls.get(0));
        }

        return insertPostInTransaction(postDTO, productIdList, imageUrls);
    }

    @Override
    public Map<String, Object> getPostDetail(int postId) {
        return communityMapper.getPostDetail(postId);
    }

    @Transactional
    int insertPostInTransaction(PostDTO postDTO, List<Long> productIdList, List<String> imageUrls) {
        communityMapper.insertPost(postDTO);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                PostImageDTO postImageDTO = PostImageDTO.builder()
                        .postImageUrl(imageUrls.get(i))
                        .postId(postDTO.getPostId())
                        .postImageOrder(i)
                        .build();
                communityMapper.insertPostImage(postImageDTO);
            }
        }

        if (productIdList != null && !productIdList.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("postId", postDTO.getPostId());
            map.put("productIds", new ArrayList<>(new HashSet<>(productIdList)));
            communityMapper.insertPostTags(map);
        }

        return postDTO.getPostId();
    }

    @Override
    public List<ProductTag> getProductTagsByPostId(int postId) {
        List<ProductTag> productTags = communityMapper.getProductTagsByPostId(postId);
        return productTags != null ? productTags : Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getOtherStylesByUserId(int userId, int excludePostId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("excludePostId", excludePostId);
        List<Map<String, Object>> otherStyles = communityMapper.getOtherStylesByUserId(params);
        return otherStyles != null ? otherStyles : Collections.emptyList();
    }

    @Override
    public List<PostImageDTO> getPostImagesByPostId(int postId) {
        List<PostImageDTO> postImages = communityMapper.getPostImagesByPostId(postId);
        return postImages != null ? postImages : Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getAllPosts(String styleCategory) {
        return communityMapper.getAllPosts(styleCategory);
    }

    @Override
    public PostDTO getPostById(int id) {
        return communityMapper.getPostById(id);
    }

    @Override
    @Transactional
    public void updatePost(PostDTO postDTO, List<Long> productIdList, List<MultipartFile> images) throws IOException {
        if (productIdList != null && !productIdList.isEmpty()) {
            List<Long> newProductIds = new ArrayList<>(new HashSet<>(productIdList));
            List<ProductTag> existingTags = communityMapper.getProductTagsByPostId(postDTO.getPostId());
            List<Long> existingProductIds = existingTags.stream()
                    .map(ProductTag::getProductId)
                    .toList();

            List<Long> tagsToAdd = newProductIds.stream()
                    .filter(id -> !existingProductIds.contains(id))
                    .toList();

            if (!tagsToAdd.isEmpty()) {
                Map<String, Object> addMap = new HashMap<>();
                addMap.put("postId", postDTO.getPostId());
                addMap.put("productIds", tagsToAdd);
                communityMapper.insertPostTags(addMap);
            }
        }

        if (images != null && !images.isEmpty()) {
            List<PostImageDTO> existingImages = communityMapper.getPostImagesByPostId(postDTO.getPostId());
            int nextImageOrder = existingImages.size();

            List<String> newImageUrls;
            if (images.size() == 1 && !images.get(0).isEmpty()) {
                String imageUrl = s3Service.uploadFile(images.get(0), "community");
                newImageUrls = Collections.singletonList(imageUrl);
            } else {
                newImageUrls = s3Service.uploadFiles(images, "community");
            }

            for (int i = 0; i < newImageUrls.size(); i++) {
                PostImageDTO postImageDTO = PostImageDTO.builder()
                        .postImageUrl(newImageUrls.get(i))
                        .postId(postDTO.getPostId())
                        .postImageOrder(nextImageOrder + i)
                        .build();
                communityMapper.insertPostImage(postImageDTO);
            }

            if (!newImageUrls.isEmpty() && (postDTO.getThumbnailImageUrl() == null || postDTO.getThumbnailImageUrl().isEmpty())) {
                postDTO.setThumbnailImageUrl(newImageUrls.get(0));
            }
        }

        communityMapper.updatePost(postDTO, productIdList, null);
    }

    @Override
    public List<PostDTO> findByStyleCategory(String styleCategory) {
        return communityMapper.findByStyleCategory(styleCategory);
    }

    @Override
    @Transactional
    public void deletePost(int postId) {
        List<PostImageDTO> images = communityMapper.getPostImagesByPostId(postId);
        for (PostImageDTO image : images) {
            s3Service.deleteFile(image.getPostImageUrl());
        }

        communityMapper.deletePost(postId);
    }

    @Override
    @Transactional
    public PostDTO insertPostLike(PostLikeDTO postLikeDTO) {
        if (!communityMapper.existsPostLike(postLikeDTO)) {
            communityMapper.insertPostLike(postLikeDTO);
            int likeCount = communityMapper.countPostLikes(postLikeDTO.getPostId());
            communityMapper.updateLikeCount(Map.of("postId", postLikeDTO.getPostId(), "likeCount", likeCount));
        }
        return communityMapper.getPostById(postLikeDTO.getPostId());
    }

    @Override
    @Transactional
    public PostDTO deletePostLike(PostLikeDTO postLikeDTO) {
        if (communityMapper.existsPostLike(postLikeDTO)) {
            communityMapper.deletePostLike(postLikeDTO);
            int likeCount = communityMapper.countPostLikes(postLikeDTO.getPostId());
            communityMapper.updateLikeCount(Map.of("postId", postLikeDTO.getPostId(), "likeCount", likeCount));
        }
        return communityMapper.getPostById(postLikeDTO.getPostId());
    }

    @Override
    public int countPostLikes(int postId) {
        return communityMapper.countPostLikes(postId);
    }

    @Override
    public boolean isLiked(int postId, int userId) {
        PostLikeDTO postLikeDTO = PostLikeDTO.builder()
                .postId(postId)
                .userId(userId)
                .build();
        return communityMapper.existsPostLike(postLikeDTO);
    }

    @Override
    @Transactional
    public PostDTO insertPostSave(PostSaveDTO postSaveDTO) {
        if (!communityMapper.existsPostSave(postSaveDTO)) {
            communityMapper.insertPostSave(postSaveDTO);
            int saveCount = communityMapper.countPostSaves(postSaveDTO.getPostId());
            communityMapper.updateSaveCount(Map.of("postId", postSaveDTO.getPostId(), "saveCount", saveCount));
        }
        return communityMapper.getPostById(postSaveDTO.getPostId());
    }

    @Override
    @Transactional
    public PostDTO deletePostSave(PostSaveDTO postSaveDTO) {
        if (communityMapper.existsPostSave(postSaveDTO)) {
            communityMapper.deletePostSave(postSaveDTO);
            int saveCount = communityMapper.countPostSaves(postSaveDTO.getPostId());
            communityMapper.updateSaveCount(Map.of("postId", postSaveDTO.getPostId(), "saveCount", saveCount));
        }
        return communityMapper.getPostById(postSaveDTO.getPostId());
    }

    @Override
    public int countPostSaves(int postId) {
        return communityMapper.countPostSaves(postId);
    }

    @Override
    public boolean isSaved(int postId, int userId) {
        PostSaveDTO postSaveDTO = PostSaveDTO.builder()
                .postId(postId)
                .userId(userId)
                .build();
        return communityMapper.existsPostSave(postSaveDTO);
    }

    @Override
    public void addFollow(int followerId, int followingId) {
        FollowDTO followDTO = FollowDTO.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        communityMapper.addFollow(followDTO);
    }

    @Override
    public void deleteFollow(int followerId, int followingId) {
        FollowDTO followDTO = FollowDTO.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        communityMapper.deleteFollow(followDTO);
    }

    @Override
    public boolean isFollowing(int followerId, int followingId) {
        FollowDTO followDTO = FollowDTO.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        return communityMapper.isFollowing(followDTO);
    }
    @Override
    public Map<String, Object> getUserProfile(int userId) {
        return communityMapper.getUserProfile(userId);
    }

    @Override
    public List<Map<String, Object>> getPostsByUserId(int userId) {
        return communityMapper.getPostsByUserId(userId);
    }


    @Override
    public List<Map<String, Object>> getSavedPostsByUserId(int userId) {
        return communityMapper.getSavedPostsByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getLikedPostsByUserId(int userId) {
        return communityMapper.getLikedPostsByUserId(userId);
    }

    @Override
    public int getFollowerCount(int userId) {
        return communityMapper.getFollowerCount(userId);
    }

    @Override
    public int getFollowingCount(int userId) {
        return communityMapper.getFollowingCount(userId);
    }

    @Override
    public List<Map<String, Object>> getTopLikedPosts(int limit) {
        List<Map<String, Object>> posts = communityMapper.getTopLikedPosts(limit);
        return posts != null ? posts : Collections.emptyList();
    }
}