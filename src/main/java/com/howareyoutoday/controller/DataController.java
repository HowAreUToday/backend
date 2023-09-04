package com.howareyoutoday.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.howareyoutoday.service.DataService;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.howareyoutoday.model.Daily;
import com.howareyoutoday.model.Member;

@RequiredArgsConstructor
@Controller
@RequestMapping("/data")
public class DataController {
    @Autowired
    private DataService dataService;

    @GetMapping("/home")
    public ResponseEntity<String> homeGetRequest(
            @CookieValue(value = "userId") String userId) {
        Member member = dataService.checkToken(userId); // 토큰으로 멤버 정보 가져오기

        if (member == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"fail find member\", \"status\": \"204\"}");
        }

        List<Daily> daily = dataService.getHomeDaily(member.getID());
        ;

        // Map<String, Object> memberentry = new HashMap<>();
        // memberentry.put("name", member.getName());
        // memberentry.put("imgPath", member.getImgPath());

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> groupedData = new HashMap<>();
            groupedData.put("id", member.getID());
            groupedData.put("name", member.getName());
            groupedData.put("mood", member.getMemo());
            groupedData.put("img", member.getImgPath());
            groupedData.put("daily", daily);

            String json = objectMapper.writeValueAsString(groupedData);

            return ResponseEntity.ok(json);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        }
    }

    @GetMapping("{Day}/daily")
    public ResponseEntity<String> dailyGetRequest(@PathVariable("Day") String Day,
            @CookieValue(value = "userId") String userId) {
        Member member = dataService.checkToken(userId); // 토큰으로 멤버 정보 가져오기

        if (member == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"fail find member\", \"status\": \"204\"}");
        }

        Daily daily = dataService.getDaily(member.getID(), Day);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> groupedData = new HashMap<>();
            groupedData.put("day", daily.getDay());
            groupedData.put("id", daily.getID());
            groupedData.put("text", daily.getText());

            String json = objectMapper.writeValueAsString(groupedData);

            return ResponseEntity.ok(json);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        }
    }

    @PostMapping("/checkDaily")
    public ResponseEntity<String> dailyGetRequest(
            @CookieValue(value = "userId") String userId,
            @RequestBody Map<String, String> requestBody) {
        Member member = dataService.checkToken(userId); // 토큰으로 멤버 정보 가져오기

        String id = requestBody.get("id");
        String day = requestBody.get("day");

        if (member == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"fail find member\", \"status\": \"204\"}");
        }

        Daily daily = dataService.getDaily(Integer.parseInt(id), day);

        if (daily == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        }
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> groupedData = new HashMap<>();

            groupedData.put("text", daily.getText());
            String json = objectMapper.writeValueAsString(groupedData);

            return ResponseEntity.ok(json);

        } catch (

        JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        }
    }

    @PostMapping("chatAI")
    public ResponseEntity<String> chatAIGetRequest(
            @CookieValue(value = "userId") String userId,
            @RequestBody Map<String, String> requestBody) {
        Member member = dataService.checkToken(userId); // 토큰으로 멤버 정보 가져오기

        String userMessage = requestBody.get("userMessage");
        String day = requestBody.get("day");

        if (member == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"fail find member\", \"status\": \"204\"}");
        }

        String AItext = dataService.getChatAI(member.getID(), userMessage, day).join();

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> groupedData = new HashMap<>();
            groupedData.put("text", AItext);

            String json = objectMapper.writeValueAsString(groupedData);

            return ResponseEntity.ok(json);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        }
    }

    @GetMapping("{Id}/{Day}/makedaily")
    public ResponseEntity<String> makedailyGetRequest(@PathVariable("Day") String Day,
            @PathVariable("Id") String id,
            @CookieValue(value = "userId") String userId) {
        Member member = dataService.checkToken(userId); // 토큰으로 멤버 정보 가져오기

        if (member == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("{\"message\": \"fail find member\", \"status\": \"204\"}");
        }

        Boolean check = dataService.makeDaily(id, Day, userId);

        if (check == true) {
            return ResponseEntity.ok("성공 메시지를 여기에 추가하세요");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        }
    }

    @PostMapping("/cahtAgain")
    public ResponseEntity<String> chatAgainGetRequest(
            @CookieValue(value = "userId") String userId,
            @RequestBody Map<String, String> requestBody) {
        Member member = dataService.checkToken(userId); // 토큰으로 멤버 정보 가져오기

        String id = requestBody.get("id");
        String day = requestBody.get("day");

        if (member == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"fail find member\", \"status\": \"204\"}");
        }

        boolean check = dataService.removeDaily(Integer.parseInt(id), day);

        if (check == true) {
            return ResponseEntity.ok("200");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        }
    }

}
