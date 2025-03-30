package kr.co.fitzcode.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.fitzcode.admin.service.ProductDetailService;
import kr.co.fitzcode.common.dto.ProductDetailDTO;
import kr.co.fitzcode.common.dto.QnaDTO;
import kr.co.fitzcode.common.dto.ReviewDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Tag(name = "Product Detail API", description = "관리자 상품 상세 관리 API 제공, 상품 정보 수정, 리뷰 및 Q&A 관리, 이미지 수정")
public class ProductDetailController {

    private final ProductDetailService productDetailService;

    // 상품 상세 정보 페이지
    @Operation(summary = "상품 상세 정보 페이지", description = "상품 ID로 상세 정보 페이지 로드")
    @GetMapping("/{productId}")
    public String getProductDetail(@Parameter @PathVariable Long productId, Model model) {
        ProductDetailDTO productDetail = productDetailService.getProductDetail(productId);

        if (productDetail == null) {
            throw new RuntimeException("상품을 찾을 수 없습니다. ID: " + productId);
        }

        model.addAttribute("productDetail", productDetail);
        return "admin/productDetail";
    }

    // 할인 가격 + 사이즈 재고 수정
    @Operation(summary = "할인 가격 + 사이즈 재고 수정", description = "상품 할인 가격 및 사이즈 재고 수정")
    @PostMapping("/{productId}/update")
    public String updateProduct(
            @Parameter @PathVariable Long productId,
            @Parameter @ModelAttribute("productDetail") ProductDetailDTO productDetail) {
        if (productDetail.getDiscountedPrice() != null) {
            productDetailService.updateDiscountedPrice(productId, productDetail.getDiscountedPrice());
        }
        if (productDetail.getSizes() != null && !productDetail.getSizes().isEmpty()) {
            productDetailService.updateSizes(productId, productDetail.getSizes());
        }
        return "redirect:/admin/products/" + productId;
    }

    // 상품 상태 업데이트
    @Operation(summary = "상품 상태 업데이트", description = "상품 상태 변경")
    @PostMapping("/{productId}/update-status")
    @ResponseBody
    public String updateStatus(@Parameter @PathVariable Long productId, @Parameter @RequestBody Map<String, Integer> request) {
        Integer newStatus = request.get("status");
        if (newStatus == null || (newStatus != 1 && newStatus != 2 && newStatus != 3)) {
            return "잘못된 상태 값입니다.";
        }
        productDetailService.updateStatus(productId, newStatus);
        return "success";
    }

    // 상품 삭제
    @Operation(summary = "상품 삭제", description = "상품 삭제 후 상품 목록 페이지로 리다이렉션")
    @PostMapping("/{productId}/delete")
    public String deleteProduct(@Parameter @PathVariable Long productId, RedirectAttributes redirectAttributes) {
        try {
            productDetailService.deleteProduct(productId);
            redirectAttributes.addFlashAttribute("successMessage", "상품이 삭제되었습니다.");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "상품 삭제 실패: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }

    // 리뷰 관리 페이지
    @Operation(summary = "리뷰 관리 페이지", description = "상품 ID로 리뷰 관리 페이지 로드")
    @GetMapping("/review/{productId}")
    public String getReviewPage(
            @Parameter @PathVariable("productId") Long productId,
            @Parameter @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        int pageSize = 7;
        int offset = (page - 1) * pageSize;

        List<ReviewDTO> reviews = productDetailService.getReviewsByProductId(productId, offset, pageSize);
        int totalReviews = productDetailService.getReviewCountByProductId(productId);
        int totalPages = (int) Math.ceil((double) totalReviews / pageSize);

        model.addAttribute("reviews", reviews);
        model.addAttribute("productId", productId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "admin/reviewManagement";
    }

    // 리뷰 삭제
    @Operation(summary = "리뷰 삭제", description = "리뷰 ID로 특정 리뷰 삭제")
    @PostMapping("/review/delete/{reviewId}")
    @ResponseBody
    public String deleteReview(@Parameter @PathVariable Long reviewId) {
        try {
            productDetailService.deleteReview(reviewId);
            return "success";
        } catch (Exception e) {
            return "리뷰 삭제 실패: " + e.getMessage();
        }
    }

    // Q&A 관리 페이지
    @Operation(summary = "Q&A 관리 페이지", description = "상품 ID로 Q&A 관리 페이지 로드")
    @GetMapping("/qna/{productId}")
    public String getQnaPage(
            @Parameter @PathVariable Long productId,
            @Parameter @RequestParam(value = "filter", defaultValue = "all") String filter,
            @Parameter @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        int pageSize = 7;
        int offset = (page - 1) * pageSize;

        List<QnaDTO> qnas = productDetailService.getQnasByProductId(productId, filter, offset, pageSize);
        int totalQnas = productDetailService.getQnaCountByProductId(productId, filter);
        int totalPages = (int) Math.ceil((double) totalQnas / pageSize);

        model.addAttribute("qnas", qnas);
        model.addAttribute("productId", productId);
        model.addAttribute("filter", filter);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "admin/qnaManagement";
    }

    // Q&A 답변 추가
    @Operation(summary = "Q&A 답변 추가", description = "Q&A ID로 답변 추가")
    @PostMapping("/qna/{productId}/answer")
    @ResponseBody
    public String addQnaAnswer(@Parameter @PathVariable Long productId, @Parameter @RequestBody QnaDTO qnaDTO) {
        try {
            productDetailService.addQnaAnswer(qnaDTO.getQnaId(), qnaDTO.getAnswer());
            return "success";
        } catch (Exception e) {
            return "답변 추가 실패: " + e.getMessage();
        }
    }

    // Q&A 답변 수정
    @Operation(summary = "Q&A 답변 수정", description = "Q&A ID로 답변 수정")
    @PostMapping("/qna/{productId}/update-answer")
    @ResponseBody
    public String updateQnaAnswer(@Parameter @PathVariable Long productId, @Parameter @RequestBody QnaDTO qnaDTO) {
        try {
            productDetailService.updateQnaAnswer(qnaDTO.getQnaId(), qnaDTO.getAnswer());
            return "success";
        } catch (Exception e) {
            return "답변 수정 실패: " + e.getMessage();
        }
    }

    // Q&A 삭제
    @Operation(summary = "Q&A 삭제", description = "Q&A ID로 특정 Q&A 삭제")
    @PostMapping("/qna/delete/{qnaId}")
    @ResponseBody
    public String deleteQna(@Parameter @PathVariable Long qnaId) {
        try {
            productDetailService.deleteQna(qnaId);
            return "success";
        } catch (Exception e) {
            return "Q&A 삭제 실패: " + e.getMessage();
        }
    }

    // 이미지 수정 처리
    @Operation(summary = "이미지 수정 처리", description = "상품의 메인 이미지 및 추가 이미지 수정")
    @PostMapping("/{productId}/update-images")
    public String updateProductImages(
            @Parameter @PathVariable Long productId,
            @Parameter @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @Parameter @RequestParam(value = "additionalImages", required = false) List<MultipartFile> additionalImages,
            @Parameter @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
            RedirectAttributes redirectAttributes) {
        try {
            productDetailService.updateProductImages(productId, mainImage, additionalImages, deleteImageIds);
            redirectAttributes.addFlashAttribute("message", "이미지가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
//            log.error("이미지 수정 실패: productId={}, error={}", productId, e.getMessage(), e); // 상세 로그
            redirectAttributes.addFlashAttribute("error", "이미지 수정 실패: " + e.getMessage());
        }
        return "redirect:/admin/products/" + productId;
    }
}