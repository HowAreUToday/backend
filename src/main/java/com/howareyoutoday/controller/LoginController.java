package com.howareyoutoday.controller;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletResponse;

import com.howareyoutoday.service.KaKaoService;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Controller
@RequestMapping("/member")
public class LoginController {

	// @Value("${spring.datasource.url}")
	// String URL;

	@Value("${my.loginUrl}")
	String LOGIN_URL;

	@Value("${kakao.restApiKey}")
	String restApiKey;

	@Value("${kakao.redirectUri}")
	String redirectUri;

	@Autowired
	KaKaoService ks;

	@GetMapping("/do")
	public String loginPage() {
		return "kakaoCI/login";
	}

	@GetMapping("/goKaKaoLogin")
	public String goKakao() {
		redirectUri = redirectUri + "&response_type=code";
		String kakaoLoginLink = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + restApiKey
				+ "&redirect_uri=" + redirectUri;
		return "redirect:" + kakaoLoginLink;
	}

	@GetMapping("/kakao")
	public String getCI(@RequestParam String code, Model model, HttpServletResponse response) throws IOException {
		System.out.println("code" + code);
		String access_token = ks.getToken(code);
		Map<String, String> userInfo = ks.getUserInfo(access_token);
		model.addAttribute("code", code);
		model.addAttribute("access_token", access_token);
		model.addAttribute("userInfo", userInfo);

		String userCookie = UUID.randomUUID().toString();

		Integer userId = ks.emailCheck(userInfo, userCookie);

		Cookie cookie = new Cookie("userId", userCookie);
		cookie.setMaxAge(-1);
		cookie.setPath("/"); // 쿠키의 경로 설정
		cookie.setSecure(true); // Secure 설정 (HTTPS 연결에서만 전송)
		cookie.setHttpOnly(true); // HttpOnly 설정


		String cookieValue = String.format("%s=%s; Path=/; Secure; HttpOnly; SameSite=None", cookie.getName(), cookie.getValue());
    		response.addHeader("set-cookie", cookieValue);



		// ci는 비즈니스 전환후 검수신청 -> 허락받아야 수집 가능
		return "redirect:https://" + LOGIN_URL + userId + "/home";

	}

}
