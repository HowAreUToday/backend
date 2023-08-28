package com.gonggam.accountApp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccountAppApplicationTests {

	@Test
	void contextLoads() {
	}

}




//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Map;
//
//public class KakaoLogin {
//
//    // 카카오 REST API 정보
//    private static final String KAKAO_CLIENT_ID = "YOUR_KAKAO_CLIENT_ID";
//    private static final String REDIRECT_URI = "YOUR_REDIRECT_URI"; // 예: "http://your-website.com/kakao-callback"
//
//    public static void main(String[] args) {
//        // 카카오 로그인 인증 URL 생성
//        String authUrl = "https://kauth.kakao.com/oauth/authorize?" +
//                "client_id=" + KAKAO_CLIENT_ID +
//                "&redirect_uri=" + REDIRECT_URI +
//                "&response_type=code";
//        System.out.println("카카오 로그인 인증 URL: " + authUrl);
//
//        // 카카오 로그인 페이지로 사용자를 리다이렉트하게끔 하면 됨
//    }
//}



//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//public class KakaoLoginCallback {
//
//    // 카카오 REST API 정보
//    private static final String KAKAO_CLIENT_ID = "YOUR_KAKAO_CLIENT_ID";
//    private static final String KAKAO_CLIENT_SECRET = "YOUR_KAKAO_CLIENT_SECRET";
//    private static final String REDIRECT_URI = "YOUR_REDIRECT_URI"; // 예: "http://your-website.com/kakao-callback"
//
//    public static void main(String[] args) {
//        String authorizationCode = "RECEIVED_AUTH_CODE"; // 콜백 URL로 받은 Authorization Code
//
//        // AccessToken 발급 요청을 위한 URL 생성
//        String tokenRequestUrl = "https://kauth.kakao.com/oauth/token?" +
//                "grant_type=authorization_code" +
//                "&client_id=" + KAKAO_CLIENT_ID +
//                "&client_secret=" + KAKAO_CLIENT_SECRET +
//                "&redirect_uri=" + REDIRECT_URI +
//                "&code=" + authorizationCode;
//
//        try {
//            // AccessToken 발급 요청
//            URL url = new URL(tokenRequestUrl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//
//            // 요청 결과 읽기
//            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String line;
//            StringBuilder response = new StringBuilder();
//            while ((line = br.readLine()) != null) {
//                response.append(line);
//            }
//            br.close();
//
//            // AccessToken 파싱
//            String accessToken = parseAccessToken(response.toString());
//            System.out.println("AccessToken: " + accessToken);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // JSON 응답에서 AccessToken 파싱
//    private static String parseAccessToken(String response) {
//        // JSON 파싱 로직 구현 (예시로 간단하게 문자열 처리)
//        int startIndex = response.indexOf("access_token\":\"") + 15;
//        int endIndex = response.indexOf("\"", startIndex);
//        return response.substring(startIndex, endIndex);
//    }
//}


