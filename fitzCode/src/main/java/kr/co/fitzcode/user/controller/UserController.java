package kr.co.fitzcode.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.co.fitzcode.common.dto.AccountDTO;
import kr.co.fitzcode.common.dto.EmailMessageDTO;
import kr.co.fitzcode.common.dto.UserDTO;
import kr.co.fitzcode.user.service.EmailService;
import kr.co.fitzcode.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    // base-url
    @Value("${app.base-url}")
    private String baseUrl;

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        if (model.containsAttribute("error")) {
            model.addAttribute("ErrorMessage", "입력한 이메일 또는 비밀번호가 잘못되었습니다.");
        }

        // 사용자가 로그인 페이지로 이동하기 전의 페이지 URL 저장 ("Referer" : 이전 페이지 URL 정보)
        String prevPage = request.getHeader("Referer");
        log.info(">>> prevPage: {}", prevPage);

        // prevPage 가 없거나 "/login"을 포함하면 기본 홈 페이지로 설정
        if (prevPage == null || prevPage.contains("/login")) {
            prevPage = baseUrl + "/"; // 동적으로 baseUrl 사용
        }
        log.info(">>> 최종 prevPage: {}", prevPage);
        request.getSession().setAttribute("prevPage", prevPage);

        return "user/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpSession session, RedirectAttributes redirectAttributes) {
        UserDTO user = userService.authenticate(email, password);
        if (user != null) {
            session.setAttribute("dto", user); // 세션에 dto 저장
            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute("error", "입력한 이메일 또는 비밀번호가 잘못되었습니다.");
        return "redirect:/login";
    }

    // 회원가입 페이지 이동
    @GetMapping("/joinForm")
    public String joinForm(Model model, UserDTO dto) {
        model.addAttribute("dto", dto);
        return "user/joinForm";
    }

    // 회원가입 이메일 임시 코드 발급
    @PostMapping("/joinForm")
    public String joinNum(@ModelAttribute("dto") UserDTO dto,
                          BindingResult bindingResult, Model model, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "user/joinForm";
        }

        // 중복 체크 (이메일, 닉네임, 전화번호)
        boolean emailCheck = userService.emailDuplicate(dto.getEmail());
        if (emailCheck) {
            model.addAttribute("emailCheckError", "이미 사용 중인 이메일 입니다.");
            return "user/joinForm";
        }
        boolean nicknameCheck = userService.nicknameDuplicate(dto.getNickname());
        if (nicknameCheck) {
            model.addAttribute("nicknameCheckError", "이미 사용 중인 닉네임 입니다.");
            return "user/joinForm";
        }
        boolean phoneNumberCheck = userService.phoneNumberDuplicate(dto.getPhoneNumber());
        if (phoneNumberCheck) {
            model.addAttribute("phoneNumberCheckError", "이미 사용 중인 전화번호 입니다.");
            return "user/joinForm";
        }

        EmailMessageDTO emailMessage = EmailMessageDTO.builder()
                .to(dto.getEmail())
                .subject("이메일 인증을 위한 인증 코드 발송")
                .build();

        String code = emailService.sendEmail(emailMessage, "user/joinEmailView");

        session.setAttribute("userDTO", dto);
        session.setAttribute("authCode", code);
        model.addAttribute("code", code);

        return "user/joinEmail";
    }

    // 회원가입 성공 GET 요청 추가했음 (회원가입 시 로그인하러 가기 리다이렉트 에러 해결)
    @GetMapping("/joinSuccess")
    public String joinSuccessGet(HttpSession session, Model model) {
        if (!model.containsAttribute("userName")) {
            return "redirect:/joinForm";
        }
        return "user/joinSuccess";
    }

    @PostMapping("/joinSuccess")
    public String joinEmailCode(@RequestParam("authCode") String authCode, HttpSession session, Model model) {
        String sessionCode = (String) session.getAttribute("authCode");
        UserDTO userDTO = (UserDTO) session.getAttribute("userDTO");

        if (sessionCode == null || userDTO == null || !Objects.equals(sessionCode, authCode)) {
            model.addAttribute("authCodeError", "인증 코드가 일치하지 않거나 세션이 만료되었습니다.");
            return "user/joinEmail";
        }

        try {
            userService.registerUser(userDTO);
            log.info("회원가입 성공 - 사용자: {}, 쿠폰 지급 완료 (쿠폰 ID: 7)", userDTO.getEmail());
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "user/joinEmail";
        }

        session.removeAttribute("authCode");
        session.removeAttribute("userDTO");

        model.addAttribute("userName", userDTO.getUserName());

        return "user/joinSuccess";
    }

    // 비밀번호 찾기 페이지 이동
    @GetMapping("/pwEmail")
    public String pwFind() {
        return "user/findpwEmail";
    }

    // 비밀번호 재설정 이메일 발송
    @PostMapping("/pwEmail")
    public String pwEmail(@RequestParam("email") String email, UserDTO dto, Model model, HttpSession session, HttpServletRequest request) {
        boolean checkEmail = userService.emailDuplicate(email);

        if (!checkEmail) {
            model.addAttribute("errorMessage", "입력한 이메일은 가입 내역이 존재하지 않습니다.");
            return "user/findpwEmail";
        }

        // 세션에 이메일과 링크 사용 여부 저장
        session.setAttribute("resetEmail", email);
        session.setAttribute("resetLinkUsed", false);

        // EmailMessageDTO에 모델 데이터 추가
        EmailMessageDTO emailMessage = EmailMessageDTO.builder()
                .to(email)
                .subject("비밀번호 재설정 메일 발송")
                .build();

        // 템플릿에 전달할 모델 데이터
        Map<String, Object> modelData = new HashMap<>();
        modelData.put("email", email);
        modelData.put("baseUrl", baseUrl);
        log.info("pwEmail 메서드에서 전달하는 email 값: {}", email);

        String code = emailService.sendEmail(emailMessage, "user/findpwEmailView", modelData);
        model.addAttribute("email", email);
        model.addAttribute("dto", dto);

        session.setAttribute("email", email);

        return "user/findpwEmailSuccess";
    }

    // 비밀번호 재설정 페이지 이동
    @GetMapping("/resetPw")
    public String resetPw(HttpSession session, UserDTO dto, Model model) {
        String email = (String) session.getAttribute("resetEmail");
        Boolean linkUsed = (Boolean) session.getAttribute("resetLinkUsed");

        // 이메일 또는 링크 사용 여부 확인
        if (email == null || linkUsed == null) {
            log.warn("세션이 만료되었습니다. 이메일: {}", email);
            model.addAttribute("errorMessage", "세션이 만료되었습니다. 이메일을 다시 입력해주세요.");
            return "redirect:/pwEmail";
        }

        // 링크가 이미 사용되었는지 확인
        if (linkUsed) {
            log.warn("이미 사용된 링크로 접근 시도 - 이메일: {}", email);
            model.addAttribute("errorMessage", "이 링크는 이미 사용되었습니다. 새로 비밀번호 재설정 링크를 요청해주세요.");
            return "redirect:/pwEmail";
        }

        // 링크 사용 처리
        session.setAttribute("resetLinkUsed", true);
        log.info("비밀번호 재설정 링크 사용 - 이메일: {}", email);

        model.addAttribute("email", email);
        model.addAttribute("dto", dto);
        log.info("비밀번호 재설정 페이지로 이동 - 이메일: {}", email);
        return "user/resetPw";
    }

    // 비밀번호 재설정
    @PostMapping("/resetPwSuccess")
    public String resetPw(@ModelAttribute("dto") UserDTO dto, BindingResult bindingResult,
                          HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            return "user/resetPw";
        }

        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            log.warn("세션이 만료되어 비밀번호 재설정 실패 - 이메일: {}", email);
            redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었습니다. 이메일을 다시 입력해주세요.");
            return "redirect:/pwEmail";
        }

        // DTO에 email 설정
        dto.setEmail(email);
        dto.setPassword(dto.getPassword());
        userService.updatePw(dto);

        // userEmail을 모델에 추가
        model.addAttribute("userEmail", email);

        // 비밀번호 재설정 후 세션에서 관련 데이터 제거
        session.removeAttribute("resetEmail");
        session.removeAttribute("resetLinkUsed");
        session.removeAttribute("email");
        log.info("비밀번호 재설정 완료 - 이메일: {}", email);

        return "user/resetPwSuccess";
    }

    @GetMapping("findEmail")
    public String findEmail() {
        return "user/findEmail";
    }

    @PostMapping("findEmailSuccess")
    public String findEmailSuccess(@RequestParam("userName") String userName,
                                   @RequestParam("phoneNumber") String phoneNumber,
                                   Model model) {
        // 입력 받은 이름과 전화번호를 사용하여 이메일 조회
        String email = userService.findEmailByNameAndPhoneNumber(userName, phoneNumber);

        if (email != null) {
            model.addAttribute("email", email);
            model.addAttribute("userName", userName);
            log.info("이메일 찾기 성공 - 사용자: {}, 이메일: {}", userName, email);
            return "user/findEmailSuccess";
        } else {
            log.warn("이메일 찾기 실패 - 사용자: {}, 전화번호: {}", userName, phoneNumber);
            model.addAttribute("errorMessage", "해당 전화번호와 이름에 대한 이메일을 찾을 수 없습니다.");
            return "redirect:/user/findEmail";
        }
    }
}