package kr.co.fitzcode.community.controller;

import groovy.util.logging.Slf4j;
import jakarta.servlet.http.HttpSession;
import kr.co.fitzcode.common.dto.*;
import kr.co.fitzcode.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
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
            Model model) {
        String styleCategory = category.equals("All") ? null : category;
        List<Map<String, Object>> posts = communityService.getAllPosts(styleCategory);
        model.addAttribute("posts", posts);
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
        // 게시물 정보 조회
        Map<String, Object> post = communityService.getPostDetail(postId);
        if (post == null) {
            return "error";
        }

        List<ProductTag> productTags = communityService.getProductTagsByPostId(postId);
        List<Map<String, Object>> otherStyles = communityService.getOtherStylesByUserId((Integer) post.get("user_id"), postId);
        List<PostImageDTO> postImages = communityService.getPostImagesByPostId(postId);
        PostDTO dto = communityService.getPostById(postId);

        UserDTO userDTO = (UserDTO) session.getAttribute("dto");

        System.out.println("Post user_id>>>>>>>>>>>>>> "+ dto.getUserId());

        model.addAttribute("post", post);
        model.addAttribute("productTags", productTags);
        model.addAttribute("otherStyles", otherStyles);
        model.addAttribute("postImages", postImages);
        model.addAttribute("currentUser", userDTO);
        model.addAttribute("username", post.get("user_name"));
        model.addAttribute("profileImage", post.get("profile_image"));
        model.addAttribute("isLiked", communityService.isLiked(postId, userDTO != null ? userDTO.getUserId() : 0));
        model.addAttribute("likeCount", communityService.countPostLikes(postId));
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


    // 좋아요 추가
    @PostMapping("/like/{postId}")
    public boolean likePost(@PathVariable int postId, @RequestBody PostLikeDTO postLikeDTO,
                            HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        postLikeDTO.setPostId(postId);
        postLikeDTO.setUserId(userDTO.getUserId());
        return communityService.insertPostLike(postLikeDTO);
    }

    // 좋아요 삭제
    @PostMapping("/unlike/{postId}")
    public boolean unlikePost(@PathVariable int postId, @RequestBody PostLikeDTO postLikeDTO,
                              HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("dto");
        postLikeDTO.setPostId(postId);
        postLikeDTO.setUserId(userDTO.getUserId());
        return communityService.deletePostLike(postLikeDTO);
    }
}