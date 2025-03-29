package kr.co.fitzcode.community.controller;

import jakarta.servlet.http.HttpSession;
import kr.co.fitzcode.common.dto.PostDTO;
import kr.co.fitzcode.common.dto.PostLikeDTO;
import kr.co.fitzcode.common.dto.PostSaveDTO;
import kr.co.fitzcode.common.dto.ProductTag;
import kr.co.fitzcode.common.dto.UserDTO;
import kr.co.fitzcode.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityApiController {

    private final CommunityService communityService;

    @GetMapping("/search-products")
    public ResponseEntity<List<ProductTag>> searchProductsByName(@RequestParam String productName) {
        List<ProductTag> products = communityService.searchProductsByName(productName);
        return ResponseEntity.ok(products);
    }

    // 좋아요 추가
    @PostMapping("/like/{postId}")
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable int postId,
                                                        @RequestBody PostLikeDTO postLikeDTO,
                                                        HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");

        if (userDTO == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(response);
        }

        postLikeDTO.setPostId(postId);
        postLikeDTO.setUserId(userDTO.getUserId());
        PostDTO updatedPost = communityService.insertPostLike(postLikeDTO);

        response.put("success", true);
        response.put("likeCount", updatedPost.getLikeCount());
        return ResponseEntity.ok(response);
    }

    // 좋아요 삭제
    @PostMapping("/unlike/{postId}")
    public ResponseEntity<Map<String, Object>> unlikePost(@PathVariable int postId,
                                                          @RequestBody PostLikeDTO postLikeDTO,
                                                          HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");

        if (userDTO == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(response);
        }

        postLikeDTO.setPostId(postId);
        postLikeDTO.setUserId(userDTO.getUserId());
        PostDTO updatedPost = communityService.deletePostLike(postLikeDTO);

        response.put("success", true);
        response.put("likeCount", updatedPost.getLikeCount());
        return ResponseEntity.ok(response);
    }

    // 북마크 추가
    @PostMapping("/save/{postId}")
    public ResponseEntity<Map<String, Object>> savePost(@PathVariable int postId,
                                                        @RequestBody PostSaveDTO postSaveDTO,
                                                        HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");

        if (userDTO == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(response);
        }

        postSaveDTO.setPostId(postId);
        postSaveDTO.setUserId(userDTO.getUserId());
        PostDTO updatedPost = communityService.insertPostSave(postSaveDTO);

        response.put("success", true);
        response.put("saveCount", updatedPost.getSaveCount());
        return ResponseEntity.ok(response);
    }

    // 북마크 삭제
    @PostMapping("/unsave/{postId}")
    public ResponseEntity<Map<String, Object>> unsavePost(@PathVariable int postId,
                                                          @RequestBody PostSaveDTO postSaveDTO,
                                                          HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");

        if (userDTO == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(response);
        }

        postSaveDTO.setPostId(postId);
        postSaveDTO.setUserId(userDTO.getUserId());
        PostDTO updatedPost = communityService.deletePostSave(postSaveDTO);

        response.put("success", true);
        response.put("saveCount", updatedPost.getSaveCount());
        return ResponseEntity.ok(response);
    }

    // 팔로우 추가
    @PostMapping("follow/{followingId}")
    public Map<String, Object> followUser(@PathVariable("followingId") int followingId, HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        if (userDTO == null) {
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        int followerId = userDTO.getUserId();
        if (followerId == followingId) {
            return Map.of("success", false, "message", "자신을 팔로우할 수 없습니다.");
        }

        try {
            communityService.addFollow(followerId, followingId);
            return Map.of("success", true, "isFollowing", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", "이미 팔로우한 사용자입니다.");
        }
    }


    // 팔로우 삭제
    @DeleteMapping("unfollow/{followingId}")
    public Map<String, Object> unfollowUser(@PathVariable("followingId") int followingId, HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        if (userDTO == null) {
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        int followerId = userDTO.getUserId();
        communityService.deleteFollow(followerId, followingId);
        return Map.of("success", true, "isFollowing", false);
    }

    // 팔로우 여부 확인
    @GetMapping("follow/check/{followingId}")
    public Map<String, Object> checkFollow(@PathVariable("followingId") int followingId, HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        if (userDTO == null) {
            return Map.of("success", false, "isFollowing", false);
        }

        int followerId = userDTO.getUserId();
        boolean isFollowing = communityService.isFollowing(followerId, followingId);
        return Map.of("success", true, "isFollowing", isFollowing);
    }
}