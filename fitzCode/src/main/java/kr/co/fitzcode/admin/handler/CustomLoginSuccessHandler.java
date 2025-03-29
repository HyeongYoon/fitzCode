package kr.co.fitzcode.admin.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.co.fitzcode.common.dto.UserDTO;
import kr.co.fitzcode.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info(">>> CustomLoginSuccessHandler 호출");

        // 사용자 이메일로 UserDTO 조회
        String email = authentication.getName();
        UserDTO userDTO = userService.findByEmail(email);

        // 세션에 사용자 정보 저장
        if (userDTO != null) {
            HttpSession session = request.getSession();
            session.setAttribute("dto", userDTO);
            log.info(">>> 세션에 사용자 저장: {}, roleId: {}", userDTO.getUserName(), userDTO.getRoleId());
        } else {
            log.warn(">>> 사용자 정보를 찾을 수 없음: {}", email);
        }

        // 리디렉션 처리
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest != null) { // 접근 권한이 없는 경로로 직접 접근한 경우
            String targetUrl = savedRequest.getRedirectUrl();
            log.info(">>> redirectUrl: {}", targetUrl);
            redirectStrategy.sendRedirect(request, response, targetUrl);
        } else { // 로그인 페이지에서 접근한 경우
            HttpSession session = request.getSession();
            String prevPage = (String) session.getAttribute("prevPage");

            // prevPage가 "/joinSuccess" 또는 "/resetPwSuccess"인 경우 "/"로 리다이렉션
            String redirectUrl = "/";
            if (prevPage != null && !prevPage.isEmpty()) {
                if (!prevPage.contains("/joinSuccess") && !prevPage.contains("/resetPwSuccess")) {
                    redirectUrl = prevPage;
                }
            }

            log.info(">>> prevPage redirectUrl: {}", redirectUrl);
            redirectStrategy.sendRedirect(request, response, redirectUrl);
        }
    }
}