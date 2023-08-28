package com.howareyoutoday.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.howareyoutoday.service.KaKaoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Controller
@Slf4j
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
	public String getCI(@RequestParam String code, Model model) throws IOException {
		String access_token = ks.getToken(code);
		Map<String, Object> userInfo = ks.getUserInfo(access_token);
		model.addAttribute("code", code);
		model.addAttribute("access_token", access_token);
		model.addAttribute("userInfo", userInfo);

		// ci는 비즈니스 전환후 검수신청 -> 허락받아야 수집 가능
		return "redirect:http://" + LOGIN_URL;
	}

	@PostMapping(path = "/emailCheck", produces = "application/json", consumes = "application/json")
	public ResponseEntity<JSONObject> emailCheck(@RequestBody String inputjson) {
		/*
		 * res = {"id":2961199691,"connected_at":"2023-08-12T09:13:22Z","properties":{
		 * "nickname":"장지원"},
		 * "kakao_account":{"profile_nickname_needs_agreement":false,"profile":{
		 * "nickname":"장지원"},
		 * "has_email":true,"email_needs_agreement":false,"is_email_valid":true,
		 * "is_email_verified":true,
		 * "email":"xks642@naver.com","has_age_range":true,"age_range_needs_agreement":
		 * false,"age_range":"20~29"}}
		 */
		JSONObject jsonObject = new JSONObject();
		HttpHeaders headers = new HttpHeaders();
		ObjectMapper objectMapper;
		JsonNode jsonNode;
		String email;
		String token;
		String isMem;
		Cookie cookie;

		try {
			objectMapper = new ObjectMapper();
			jsonNode = objectMapper.readTree(inputjson);
			email = jsonNode.get("email").asText();
			// isMem = memberService.checkEmail(email);// checkEmail로 변경하기

			// if (isMem == "no") { // 신규
			// if (!signup(inputjson)) {
			// jsonObject.put("status", "500");
			// jsonObject.put("message", "fail sigup");
			// return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonObject);
			// }
			// } else if (isMem == "error") {
			// jsonObject.put("status", "500");
			// jsonObject.put("message", "fail memberService.checkEmail");
			// return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonObject);
			// }

			// token = memberService.getToken(email);
			// if (token == "error") {
			// jsonObject.put("status", "500");
			// jsonObject.put("message", "can not get token");
			// return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonObject);
			// } else {
			// jsonObject.put("message", "success");
			// jsonObject.put("status", "200");
			// }

		} catch (JsonProcessingException e) {
			e.printStackTrace();
			// jsonObject.put("status", "500");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonObject);
		}
		// cookie = new Cookie("memberId", token);
		// cookie.setMaxAge(3600);
		// headers.add(HttpHeaders.SET_COOKIE,
		// cookie.getName() + "=" + cookie.getValue() + "; path=/; secure; httpOnly;
		// SameSite=None");
		return ResponseEntity.ok().headers(headers).body(jsonObject);
	}

	// 회원가입
	// public boolean signup(String inputjson) {
	// try {
	// log.info("inputjson : " + inputjson);
	// ObjectMapper objectMapper = new ObjectMapper();
	// JsonNode jsonNode = objectMapper.readTree(inputjson);
	// String email = jsonNode.get("email").asText();
	// String name = jsonNode.get("name").asText();
	// String age = jsonNode.get("age").asText();
	// boolean kakao = false;
	// boolean google = false;
	// String ka_go = jsonNode.get("kakao/google").asText();

	// switch (ka_go) {
	// case "kakao":
	// kakao = true;
	// break;
	// case "google":
	// google = true;
	// break;
	// }

	// // Member member = new Member();
	// // member.setEmail(email);
	// // member.setName(name);
	// // member.setKakao(kakao);
	// // member.setGoogle(google);
	// // member.setAge(age);

	// // return memberService.addMember(member);

	// } catch (JsonProcessingException e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	// @PostMapping(path = "/getUser", produces = "application/json", consumes =
	// "application/json")
	// public ResponseEntity<JSONObject> getMember(@RequestBody String inputjson,
	// @CookieValue(value = "memberId") Cookie cookie) throws JsonMappingException,
	// JsonProcessingException {
	// JSONObject resultObj = new JSONObject();
	// String token;
	// Member member;

	// try {
	// token = cookie.getValue();
	// member = memberService.getMember(token);
	// if (member == null) {
	// resultObj.put("status", "204");
	// resultObj.put("message", "not found Member");
	// return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultObj);
	// }

	// resultObj.put("name", member.getName());
	// resultObj.put("profileImgPATH", member.getImgPath());
	// resultObj.put("profileMessage", member.getProfileMessage());

	// } catch (Exception e) {
	// e.printStackTrace();
	// resultObj.put("status", "500");
	// resultObj.put("message", "error");
	// return
	// ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultObj);
	// }
	// // System.out.println("결과 : "+token);
	// return ResponseEntity.ok().body(resultObj);
	// }

}