package kr.co.fitzcode.common.controller;

import kr.co.fitzcode.common.dto.CustomOAuth2User;
import kr.co.fitzcode.common.dto.NotificationDTO;
import kr.co.fitzcode.common.service.CustomUserDetails;
import kr.co.fitzcode.common.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/notifications")
    public ResponseEntity<String> createNotification(@RequestBody NotificationDTO notificationDTO, Principal principal) {
        log.info("POST /api/notifications 요청 처리");
        if (principal == null) {
            log.warn("Principal이 null입니다 - 인증되지 않은 접근");
            return ResponseEntity.status(401).body("{\"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
        }

        int userId = extractUserId(principal);
        notificationDTO.setUserId(userId);
        return ResponseEntity.ok("알림 생성 완료");
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(Principal principal) {
        log.info("GET /api/notifications 요청 처리");
        if (principal == null) {
            log.warn("Principal null - 인증되지 않은 접근");
            return ResponseEntity.status(401).body("{\"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
        }

        int userId = extractUserId(principal);
        log.info("userId {}에 대한 알림 조회", userId);
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable int id, Principal principal) {
        log.info("DELETE /api/notifications/{} 요청 처리", id);
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("알림 삭제 완료");
    }

    @DeleteMapping("/notifications")
    public ResponseEntity<String> deleteAllNotifications(Principal principal) {
        log.info("DELETE /api/notifications 요청 처리");
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        int userId = extractUserId(principal);
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.ok("모든 알림 삭제 완료");
    }

    @PostMapping("/notifications/read")
    public ResponseEntity<String> markAllAsRead(Principal principal) {
        log.info("POST /api/notifications/read 요청 처리");
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        int userId = extractUserId(principal);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("모든 알림 읽음 처리 완료");
    }

    // 인증 상태 확인 엔드포인트
    @GetMapping("/user/check")
    public ResponseEntity<Map<String, Boolean>> checkAuthenticated(Principal principal) {
        log.info("GET /api/user/check 요청 처리");
        Map<String, Boolean> response = new HashMap<>();
        response.put("authenticated", principal != null); // Principal이 null이 아니면 인증됨
        return ResponseEntity.ok(response);
    }

    // Principal에서 userId를 추출하는 헬퍼 메서드
    private int extractUserId(Principal principal) {
        Object principalObj = principal;

        // UsernamePasswordAuthenticationToken 또는 OAuth2AuthenticationToken 처리
        if (principalObj instanceof UsernamePasswordAuthenticationToken) {
            principalObj = ((UsernamePasswordAuthenticationToken) principalObj).getPrincipal();
        } else if (principalObj instanceof OAuth2AuthenticationToken) {
            principalObj = ((OAuth2AuthenticationToken) principalObj).getPrincipal();
        }

        // 내부 Principal 객체에서 userId 추출
        if (principalObj instanceof CustomUserDetails) {
            return ((CustomUserDetails) principalObj).getUserId();
        } else if (principalObj instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principalObj).getUserId();
        } else {
            log.error("알 수 없는 Principal 내부 타입: {}", principalObj.getClass().getName());
            throw new IllegalStateException("알 수 없는 Principal 내부 타입: " + principalObj.getClass().getName());
        }
    }
}