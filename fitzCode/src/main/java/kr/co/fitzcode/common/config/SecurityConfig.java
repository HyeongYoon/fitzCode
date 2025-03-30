package kr.co.fitzcode.common.config;

import kr.co.fitzcode.admin.handler.CustomLoginSuccessHandler;
import kr.co.fitzcode.user.service.CustomOAuth2UserService;
import kr.co.fitzcode.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> response.sendRedirect("/access-denied");
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            response.sendRedirect("/login?error=true");
        };
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityContext(securityContext ->
                        securityContext.securityContextRepository(new HttpSessionSecurityContextRepository())
                )
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests
                            .requestMatchers(
                                    "/",                            // 메인 페이지
                                    "/css/**",                      // CSS 정적 리소스
                                    "/js/**",                       // JS 정적 리소스
                                    "/img/**",                      // 이미지 정적 리소스
                                    "/favicon.ico",                 // 파비콘
                                    "/access-denied",               // 접근 거부 페이지
                                    "/404-error",                   // 404 페이지
                                    "/something",                   // 기타 페이지
                                    "/product/list/**",             // 상품 목록
                                    "/product/detail/**",           // 상품 상세
                                    "/api/product/**",              // 상품 API
                                    "/api/reviews",                 // 리뷰 조회 API (GET 요청 허용)
                                    "/login/**",                    // 로그인 관련
                                    "/logout",                      // 로그아웃
                                    "/joinForm",                    // 회원가입 폼
                                    "/joinSuccess",                 // 회원가입 성공
                                    "/pwEmail",                     // 비밀번호 이메일
                                    "/findEmail",                   // 이메일 찾기
                                    "/findEmailSuccess",            // 이메일 찾기 성공
                                    "/resetPw",                     // 비밀번호 재설정
                                    "/resetPwSuccess",              // 비밀번호 재설정 성공
                                    "/inquiry/searchProduct",       // 문의 상품 검색
                                    "/inquiry/searchOrderList",     // 문의 주문 검색
                                    "/inquiry/selectedProduct",     // 문의 선택 상품
                                    "/admin/notice/subscribe",      // 공지사항 구독
                                    "/api/notifications",           // 알림 API
                                    "/products",                    // 상품 페이지
                                    "/styles",                      // 스타일 페이지
                                    "/notice",                      // 공지사항
                                    "/notice/**",                   // 공지사항 하위 경로
                                    "/api/cart/**",                 // 카트 API
                                    "/api/user/check",              // 사용자 인증 체크
                                    "/search",                      // 검색
                                    "/search/result",               // 검색 결과
                                    "/api/pick-products",           // 주목받는 상품 API
                                    "/api/discount-products",       // 할인 상품 API
                                    "/mypage/insertAccount",
                                    "/mypage/insertAddress",
                                    "/inquiry/searchProduct",
                                    "/inquiry/searchOrderList",
                                    "/inquiry/selectedProduct",
                                    "/community/list/**",
                                    "/community/form/**",
                                    "/community/detail/**",
                                    "/api/community/search-products",
                                    "/api/community/like/**",       // 좋아요 API
                                    "/api/community/unlike/**",     // 좋아요 취소 API
                                    "/community/modify/**",
                                    "/checkEmail",                  // 이메일 중복 확인
                                    "/checkNickname",               // 닉네임 중복 확인
                                    "/community/api/styles"         // 인기 스타일 API
                            ).permitAll()
                            // 권한별 경로
                            .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/admin/coupon/**").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/admin/dashboard")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_LOGISTICS", "ROLE_INQUIRY")
                            .requestMatchers(
                                    "/admin/products",
                                    "/admin/products/*",
                                    "/admin/sales",
                                    "/admin/sales/*",
                                    "/admin/products/returns",
                                    "/admin/products/returns/*",
                                    "/admin/products/refund",
                                    "/admin/products/refund/*",
                                    "/admin/shipping",
                                    "/admin/shipping/*"
                            ).hasAnyAuthority("ROLE_ADMIN", "ROLE_LOGISTICS")
                            .requestMatchers(
                                    "/admin/inquiries",
                                    "/admin/inquiries/*",
                                    "/admin/products/qna/{productId}"
                            ).hasAnyAuthority("ROLE_ADMIN", "ROLE_INQUIRY")
                            .requestMatchers("/admin/notice", "/admin/notice/*")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_LOGISTICS")
                            .requestMatchers("/admin/products/{productId}", "/admin/products/{productId}/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_LOGISTICS")
                            .anyRequest().authenticated();
                })
                .requestCache(requestCache -> requestCache
                        .requestCache(new HttpSessionRequestCache()) // for login redirection
                )
                .formLogin(formLogin -> {
                    formLogin
                            .loginPage("/login")
                            .loginProcessingUrl("/login")
                            .usernameParameter("email")
                            .passwordParameter("password")
                            .successHandler(customLoginSuccessHandler)
                            .failureHandler(failureHandler())
                            .permitAll();
                })
                .logout(logout -> {
                    logout
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/")
                            .invalidateHttpSession(true)
                            .clearAuthentication(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll();
                })
                .sessionManagement(session -> {
                    session
                            .sessionFixation().migrateSession()
                            .maximumSessions(1)
                            .maxSessionsPreventsLogin(true)
                            .expiredUrl("/login?expired");
                })
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling
                            .authenticationEntryPoint((request, response, authException) -> {
                                String requestUri = request.getRequestURI();
                                // permitAll() 경로면 리디렉션 안함
                                if (requestUri.equals("/") || requestUri.equals("/login") || requestUri.equals("/logout") ||
                                        requestUri.startsWith("/css/") || requestUri.startsWith("/js/") ||
                                        requestUri.startsWith("/img/") ||
                                        requestUri.startsWith("/product/list/") || requestUri.startsWith("/product/detail/") ||
                                        requestUri.startsWith("/api/product/") || requestUri.startsWith("/api/reviews") ||
                                        requestUri.equals("/joinForm") || requestUri.equals("/joinSuccess") ||
                                        requestUri.equals("/pwEmail") || requestUri.equals("/findEmail") ||
                                        requestUri.equals("/findEmailSuccess") || requestUri.equals("/resetPw") ||
                                        requestUri.equals("/resetPwSuccess") || requestUri.equals("/inquiry/searchProduct") ||
                                        requestUri.equals("/inquiry/searchOrderList") || requestUri.equals("/inquiry/selectedProduct") ||
                                        requestUri.equals("/admin/notice/subscribe") || requestUri.equals("/api/notifications") ||
                                        requestUri.equals("/products") || requestUri.equals("/styles") ||
                                        requestUri.equals("/notice") || requestUri.startsWith("/api/cart/") ||
                                        requestUri.equals("/search") || requestUri.equals("/search/result") ||
                                        requestUri.equals("/api/pick-products") || requestUri.equals("/api/discount-products") ||
                                        requestUri.startsWith("/community/") || requestUri.startsWith("/api/community/")) {
                                    return;
                                }
                                response.sendRedirect("/login");
                            });
                })
                .csrf(auth -> auth.disable());

        http
                .oauth2Login(oauth2 ->
                        oauth2.loginPage("/login")
                                .successHandler(customLoginSuccessHandler)
                                .defaultSuccessUrl("/", true)
                                .userInfoEndpoint(userInfoEndpointConfig ->
                                        userInfoEndpointConfig.userService(customOAuth2UserService))
                );

        return http.build();
    }
}