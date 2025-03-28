package kr.co.fitzcode.user.service;

public interface OAuth2Response {
    // 제공자 .. naver, google, kakao
    String getProvider();
    // 제공자를 발급해주는 아이디
    String getProviderId();
    // 사용자 이메일
    String getEmail();
    // 사용자 이름 (실명) 
    String getuserName();
    // 사용자 별명
    String getNickname();
    // 사용자 생일
    String getBirthday();
    // 사용자 출생년도
    String getBirthyear();
    // 사용자 번호
    String getPhoneNumber();
    // 사용자 프로필 사진
    String getProfileImageUrl();


}
