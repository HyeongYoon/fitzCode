package kr.co.fitzcode.community.controller;

import jakarta.servlet.http.HttpSession;
import kr.co.fitzcode.common.dto.*;
import kr.co.fitzcode.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // 게시물 리스트 이동
    @GetMapping("/list")
    public String getCommunityList(
            @RequestParam(value = "category", required = false, defaultValue = "All") String category,
            Model model, HttpSession session) {
        String styleCategory = category.equals("All") ? null : category;
        List<Map<String, Object>> posts = communityService.getAllPosts(styleCategory);
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        int userId = (userDTO != null) ? userDTO.getUserId() : -1;

        for (Map<String, Object> post : posts) {
            int postId = (int) post.get("post_id");
            boolean isLiked = userId != -1 && communityService.isLiked(postId, userId);
            post.put("isLiked", isLiked);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("isLoggedIn", userDTO != null);
        return "community/communityList";
    }

    // 게시물 작성 이동
    @GetMapping("/form")
    public String form() {
        return "community/communityForm";
    }

    // 게시물 작성
    @PostMapping("/writeForm")
    public String createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam("productIds") String productIds,
            @RequestParam("styleCategory") String styleCategory,
            HttpSession session,
            RedirectAttributes redirectAttributes) throws IOException {

        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        if (userDTO == null) {
            throw new IllegalStateException("사용자 정보가 세션에 없음");
        }
        int userId = userDTO.getUserId();

        List<Long> productIdList = productIds != null && !productIds.isEmpty()
                ? Arrays.stream(productIds.split(","))
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList()
                : null;

        String styleCategoryValue = styleCategory.isEmpty() ? "" : styleCategory;

        PostDTO postDTO = PostDTO.builder()
                .userId(userId)
                .styleCategory(styleCategoryValue)
                .title(title)
                .content(content)
                .build();

        int postId = communityService.insertPost(postDTO, productIdList, images);

        redirectAttributes.addAttribute("postId", postId);
        return "redirect:/community/detail/{postId}";
    }

    // 상세 페이지 이동
    @GetMapping("/detail/{postId}")
    public String getPostDetail(@PathVariable("postId") int postId, Model model, HttpSession session) {
        Map<String, Object> post = communityService.getPostDetail(postId);
        if (post == null) {
            return "error";
        }

        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        int currentUserId = (userDTO != null) ? userDTO.getUserId() : -1;

        int postUserId = ((Number) post.get("user_id")).intValue();
        log.info("Post user_id {}", postUserId);

        boolean isOwnPost = currentUserId != -1 && currentUserId == postUserId;

        boolean isFollowing = false;
        if (!isOwnPost && currentUserId != -1) {
            isFollowing = communityService.isFollowing(currentUserId, postUserId);
        }

        List<ProductTag> productTags = communityService.getProductTagsByPostId(postId);
        List<Map<String, Object>> otherStyles = communityService.getOtherStylesByUserId(postUserId, postId);
        List<PostImageDTO> postImages = communityService.getPostImagesByPostId(postId);
        PostDTO dto = communityService.getPostById(postId);

        boolean isLiked = currentUserId != -1 && communityService.isLiked(postId, currentUserId);
        int likeCount = communityService.countPostLikes(postId);
        boolean isSaved = currentUserId != -1 && communityService.isSaved(postId, currentUserId);
        int saveCount = communityService.countPostSaves(postId);

        model.addAttribute("post", post);
        model.addAttribute("productTags", productTags);
        model.addAttribute("otherStyles", otherStyles);
        model.addAttribute("postImages", postImages);
        model.addAttribute("currentUser", userDTO);
        model.addAttribute("username", post.get("user_name"));
        model.addAttribute("profileImage", post.get("profile_image"));
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("isSaved", isSaved);
        model.addAttribute("saveCount", saveCount);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("isOwnPost", isOwnPost);

        return "community/communityDetail";
    }

    // 게시물 수정 페이지 이동
    @GetMapping("/modify/{id}")
    public String showModifyForm(@PathVariable("id") int postId, Model model) {
        PostDTO post = communityService.getPostById(postId);
        List<PostImageDTO> postImages = communityService.getPostImagesByPostId(postId);
        List<ProductTag> productTags = communityService.getProductTagsByPostId(postId);

        model.addAttribute("post", post);
        model.addAttribute("postImages", postImages);
        model.addAttribute("productTags", productTags);
        return "community/communityModify";
    }

    // 게시물 수정
    @PostMapping("/modify/{id}")
    public String modifyPost(
            @PathVariable("id") int id,
            @ModelAttribute("post") PostDTO post,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "productIds", required = false) String productIds,
            HttpSession session,
            RedirectAttributes redirectAttributes) throws IOException {

        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        if (userDTO == null) {
            throw new IllegalStateException("사용자 정보가 세션에 없음");
        }

        PostDTO postDTO1 = communityService.getPostById(id);
        if (postDTO1.getUserId() != userDTO.getUserId()) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        post.setPostId(id);
        post.setUserId(postDTO1.getUserId());

        List<Long> productIdList = productIds != null && !productIds.isEmpty()
                ? Arrays.stream(productIds.split(","))
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList()
                : null;

        communityService.updatePost(post, productIdList, images);

        redirectAttributes.addAttribute("postId", id);
        return "redirect:/community/detail/{postId}";
    }

    // 게시물 삭제
    @PostMapping("/delete/{postId}")
    public String deletePost(@PathVariable("postId") int postId) {
        communityService.deletePost(postId);
        return "redirect:/community/list";
    }

    // 좋아요 수 기준 상위 4개 스타일 조회
    @GetMapping("/api/styles")
    @ResponseBody
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<Map<String, Object>>> getTopLikedStyles(
            @RequestParam(value = "sort", defaultValue = "likes") String sort,
            @RequestParam(value = "limit", defaultValue = "4") int limit,
            HttpSession session) {
        try {
            UserDTO userDTO = (UserDTO) session.getAttribute("dto");
            int userId = (userDTO != null) ? userDTO.getUserId() : -1;

            List<Map<String, Object>> posts = communityService.getTopLikedPosts(limit);
            for (Map<String, Object> post : posts) {
                int postId = (int) post.get("post_id");
                boolean isLiked = userId != -1 && communityService.isLiked(postId, userId);
                post.put("isLiked", isLiked);
            }

            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("인기 스타일 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 사용자 프로필 조회 (두 번째 코드에서 추가된 부분)
    @GetMapping("/user/profile/{userId}")
    public String getUserProfile(@PathVariable("userId") int userId, Model model, HttpSession session) {
        Map<String, Object> userProfile = communityService.getUserProfile(userId);
        if (userProfile == null) {
            return "error";
        }

        List<Map<String, Object>> userPosts = communityService.getPostsByUserId(userId);
        List<Map<String, Object>> savedPosts = communityService.getSavedPostsByUserId(userId);
        List<Map<String, Object>> likedPosts = communityService.getLikedPostsByUserId(userId);

        UserDTO currentUserDTO = (UserDTO) session.getAttribute("dto");
        int currentUserId = (currentUserDTO != null) ? currentUserDTO.getUserId() : -1;

        boolean isFollowing = currentUserId != -1 && communityService.isFollowing(currentUserId, userId);
        boolean isOwnProfile = currentUserId != -1 && currentUserId == userId;

        int followerCount = communityService.getFollowerCount(userId);
        int followingCount = communityService.getFollowingCount(userId);

        model.addAttribute("user", userProfile);
        model.addAttribute("userPosts", userPosts);
        model.addAttribute("savedPosts", savedPosts);
        model.addAttribute("likedPosts", likedPosts);
        model.addAttribute("currentUser", currentUserDTO);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("isOwnProfile", isOwnProfile);
        model.addAttribute("followerCount", followerCount);
        model.addAttribute("followingCount", followingCount);

        return "community/userProfile";
    }
}