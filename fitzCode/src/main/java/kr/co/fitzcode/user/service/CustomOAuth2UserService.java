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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;
    private final HttpServletRequest request;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registerId = userRequest.getClientRegistration().getRegistrationId();
        log.info("registerId {}", registerId);

        HttpSession session = request.getSession();

        OAuth2Response oAuth2Response;
        String providerUserId;
        String userBirth;

        if (registerId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registerId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        providerUserId = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
        userBirth = oAuth2Response.getBirthyear() + "-" + oAuth2Response.getBirthday();

        UserDTO user = registerId.equals("naver") ? userMapper.findByUserNaverId(providerUserId) : userMapper.findByUserKakaoId(providerUserId);

        int dbUserId;
        List<String> roles;
        if (user == null) {
            UserDTO newUser = new UserDTO();
            if (registerId.equals("naver")) {
                newUser.setNaverId(providerUserId);
            } else if (registerId.equals("kakao")) {
                newUser.setKakaoId(providerUserId);
            }
            newUser.setUserName(oAuth2Response.getuserName());
            newUser.setNickname(oAuth2Response.getNickname());
            newUser.setEmail(oAuth2Response.getEmail());
            newUser.setPhoneNumber(oAuth2Response.getPhoneNumber());
            newUser.setBirthDate(userBirth);
            newUser.setProfileImage(oAuth2Response.getProfileImageUrl());
            newUser.setRoleId(UserRole.USER.getCode()); // 기본값: USER (roleId = 1)

            userMapper.insertUser(newUser);
            userMapper.insertUserTier(oAuth2Response.getNickname());
            user = userMapper.findByEmail(newUser.getEmail());
            dbUserId = user.getUserId();
            roles = Collections.singletonList(UserRole.USER.getRoleName());
        } else {
            dbUserId = user.getUserId();
            // USER_ROLE_MAPPING에서 역할 목록 가져오기
            List<Integer> roleIds = userMapper.getUserRolesByUserId(dbUserId);
            if (roleIds != null && !roleIds.isEmpty()) {
                roles = roleIds.stream()
                        .map(roleId -> UserRole.fromCode(roleId).getRoleName())
                        .collect(Collectors.toList());
            } else {
                roles = Collections.singletonList(UserRole.USER.getRoleName()); // 기본값
            }
        }

        session.setAttribute("dto", user);
        log.info("CustomOAuth2User with userId={}, roles={}", dbUserId, roles);
        return new CustomOAuth2User(oAuth2Response, roles, dbUserId);
    }
}