package kr.co.fitzcode.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.co.fitzcode.common.dto.CustomOAuth2User;
import kr.co.fitzcode.common.dto.KakaoResponse;
import kr.co.fitzcode.common.dto.NaverResponse;
import kr.co.fitzcode.common.dto.UserDTO;
import kr.co.fitzcode.common.enums.UserRole;
import kr.co.fitzcode.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;
    private final HttpServletRequest request;
    private final SwaggerIndexTransformer indexPageTransformer;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println("oAuth2User >>>>>>>>>>>>>>>>>>>>>>>" + oAuth2User);
        // 어떤 정보가 넘어오는지 확인
        String registerId = userRequest.getClientRegistration().getRegistrationId();
        System.out.println("registerId >>>>>>>>>>>>>>>>>>>>>" + registerId);

        HttpSession session = request.getSession();

        OAuth2Response oAuth2Response = null;
        String userId = null;
        String userBirth = null;

        // 여기에 카카오와 네이버 응답 추가
        if (registerId.equals("naver")) {
            // 네이버 응답
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registerId.equals("kakao")) {
            // 카카오 응답
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        userId = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
        userBirth = oAuth2Response.getBirthyear() + "-" + oAuth2Response.getBirthday();

        // 역할 부여
        UserRole role = UserRole.USER;
        int roleId = role.getCode();

        UserDTO user = null;

        if (registerId.equals("naver")) {
            user = userMapper.findByUserNaverId(userId);
        } else if (registerId.equals("kakao")) {
            user = userMapper.findByUserKakaoId(userId);
        }

        // 신규 사용자라면 db에 데이터 저장
        if (user == null) {
            UserDTO newUser = new UserDTO();
            if (registerId.equals("naver")) {
                newUser.setNaverId(userId);
            } else if (registerId.equals("kakao")) {
                newUser.setKakaoId(userId);
            }
            newUser.setUserName(oAuth2Response.getuserName());
            newUser.setNickname(oAuth2Response.getNickname());
            newUser.setEmail(oAuth2Response.getEmail());
            newUser.setPhoneNumber(oAuth2Response.getPhoneNumber());
            newUser.setBirthDate(userBirth);
            newUser.setProfileImage(oAuth2Response.getProfileImageUrl());
            newUser.setRoleId(roleId);

            userMapper.insertUser(newUser);

            // 신규 사용자 로그인 처리
            session.setAttribute("dto", newUser);
        } else {
            // 기존 사용자 로그인 처리
            session.setAttribute("dto", user);
        }

        return new CustomOAuth2User(oAuth2Response, role.getRoleName());
    }
}