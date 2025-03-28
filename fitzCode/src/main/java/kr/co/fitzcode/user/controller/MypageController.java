package kr.co.fitzcode.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.fitzcode.common.dto.*;
import kr.co.fitzcode.common.util.SecurityUtils;
import kr.co.fitzcode.user.service.MypageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {
    private final SecurityUtils securityUtils;
    private final MypageService mypageService;

    // base-url 값
    @Value("${app.base-url}")
    private String baseUrl;

    // 내 프로필
    @GetMapping("/myProfile")
    public String mypage(Model model) {
        int userId = securityUtils.getUserId();
        UserDTO userDTO = mypageService.getMyInfo(userId);
        List<OrderDTO> orderDTO = mypageService.getMypageOrderList(userId);

        model.addAttribute("userDTO", userDTO);
        model.addAttribute("orderDTO", orderDTO);
        return "user/mypage/myProfile";
    }

    // 프로필 수정 폼으로 이동
    @GetMapping("/updateProfile")
    public String updateProfile(Model model) {
        int userId = securityUtils.getUserId();
        UserDTO userDTO = mypageService.getMyInfo(userId);
        model.addAttribute("dto", userDTO);
        return "user/mypage/updateProfile";
    }

    // 프로필 수정 저장
    @PostMapping("/updateProfile")
    public String updateProfile(@ModelAttribute UserDTO userDTO,
                                @RequestParam(value = "profileImgUrl", required = false) MultipartFile profileImage) {
        mypageService.updateProfile(userDTO, profileImage);
        return "redirect:/mypage/myProfile";
    }

    // 계좌 관리
    @GetMapping("/account")
    public String account(Model model) {
        int userId = securityUtils.getUserId();
        List<AccountDTO> accountDTO = mypageService.getUserAccount(userId);
        if (accountDTO.isEmpty()) { // 처음 등록하면 계좌 등록 페이지로 이동
            model.addAttribute("userId", userId);
            return "user/mypage/insertAccount";
        } else {
            AccountDTO defaultAccount = accountDTO.stream().filter(AccountDTO::isDefault).findFirst().orElse(null);
            AccountDTO userAccount = accountDTO.stream().filter(account -> !account.isDefault()).findFirst().orElse(null);
            model.addAttribute("default", defaultAccount);
            model.addAttribute("account", userAccount);
        }
        return "user/mypage/account";
    }

    // 계좌 초기등록
    @PostMapping("/insertDefaultAccount")
    public String insertDefaultAccount(@ModelAttribute AccountDTO accountDTO) {
        mypageService.insertAccountData(accountDTO);
        return "redirect:/mypage/account";
    }

    // 계좌 추가
    @PostMapping("/insertAccount")
    public String insertAccount(@RequestBody AccountDTO accountDTO) {
        mypageService.insertAccountData(accountDTO);
        return "redirect:/mypage/account";
    }

    // 계좌 삭제
    @GetMapping("/deleteAccount/{accountId}")
    public String deleteAccount(@PathVariable("accountId") int accountId) {
        mypageService.deleteAccount(accountId);
        return "redirect:/mypage/account";
    }

    // 회원정보 수정 전 인증 페이지
    @GetMapping("/verifyUser")
    public String verifyIdentity(Model model, HttpServletRequest request) {
        int userId = securityUtils.getUserId();
        UserDTO userDTO = mypageService.getMyInfo(userId);
        model.addAttribute("dto", userDTO);

        // 로그인 인증 후 갈 페이지 session에 담기
        String targetUrl = baseUrl + "/mypage/verifiedUser";
        request.getSession().setAttribute("prevPage", targetUrl);
        return "user/mypage/verifyIdentity";
    }

    // 회원 인증 확인 후 -> 회원정보 수정 폼으로 이동
    @GetMapping("/verifiedUser")
    public String verifyUser(Model model) {
        int userId = securityUtils.getUserId();
        UserDTO userDTO = mypageService.getMyInfo(userId);
        model.addAttribute("dto", userDTO);
        return "user/mypage/updateInfo";
    }

    // 수정된 회원 정보 저장
    @PostMapping("/updateInfo")
    public String updateInfo(@ModelAttribute UserDTO userDTO) {
        log.info("userDTO: {}", userDTO.getPassword());
        mypageService.updateUserInfo(userDTO);
        return "redirect:/mypage/verifiedUser";
    }

    // 사용자 쿠폰
    @GetMapping("/mycoupon")
    public String mycoupon(Model model) {
        int userId = securityUtils.getUserId();
        List<CouponDTO> userCouponList = mypageService.getUserCoupon(userId);
        model.addAttribute("list", userCouponList);
        return "user/mypage/mycoupon";
    }
}