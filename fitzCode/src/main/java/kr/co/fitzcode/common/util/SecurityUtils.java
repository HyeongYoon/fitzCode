package kr.co.fitzcode.common.util;

import kr.co.fitzcode.common.service.CustomUserDetails;
import kr.co.fitzcode.common.dto.CustomOAuth2User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtils {

    public static int getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId(); // 일반 로그인 사용자
        } else if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUserId(); // OAuth2 로그인 사용자
        } else {
            throw new RuntimeException("Unknown user type");
        }
    }
}