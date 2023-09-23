package com.howareyoutoday.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.howareyoutoday.model.Daily;
import com.howareyoutoday.model.Member;

@Service
public class DataService {
    @Value("${spring.datasource.url}")
    String URL;

    @Value("${spring.datasource.username}")
    String USERNAME;

    @Value("${my.aiurl}")
    String AIURL;

    @Value("${spring.datasource.password}")
    String SQL_PASSWORD;

    public Member checkToken(String Token) {
        Connection connection;
        PreparedStatement selectStmt = null;
        Member member = new Member();

        try {
            connection = DriverManager.getConnection(URL, USERNAME, SQL_PASSWORD);

            // SQL 쿼리 준비
            String selectSql = "SELECT * FROM HAUTmain.Member WHERE Token = ?";
            selectStmt = connection.prepareStatement(selectSql);
            selectStmt.setString(1, Token);

            // SQL 쿼리 실행
            ResultSet resultSet = selectStmt.executeQuery();

            if (resultSet.next()) {
                member.setID(resultSet.getInt("ID"));
                member.setEmail(resultSet.getString("Email"));
                member.setName(resultSet.getString("Name"));
                member.setAge(resultSet.getString("Age"));
                member.setGender(resultSet.getString("Gender"));
                member.setImgPath(resultSet.getString("ImgPath"));
                member.setMemo(resultSet.getString("Memo"));
                member.setToken(resultSet.getString("Token"));
                return member;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Daily> getHomeDaily(int userId) {
        Connection connection;
        PreparedStatement selectStmt = null;
        List<Daily> dailyList = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(URL, USERNAME, SQL_PASSWORD);

            // SQL 쿼리 준비: 가장 최근 날짜의 5개 데이터를 가져옵니다.
            String selectSql = "SELECT * FROM HAUTmain." + userId + "_daily ORDER BY Day DESC LIMIT 5";
            selectStmt = connection.prepareStatement(selectSql);

            // SQL 쿼리 실행
            ResultSet resultSet = selectStmt.executeQuery();

            while (resultSet.next()) {
                // ResultSet에서 데이터를 읽어 Daily 객체에 매핑
                Daily daily = new Daily();
                daily.setID(resultSet.getInt("ID"));
                daily.setDay(resultSet.getString("Day"));
                daily.setText(resultSet.getString("Text"));

                // Daily 객체를 리스트에 추가
                dailyList.add(daily);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dailyList;
    }

    public Daily getDaily(int id, String day) {
        Connection connection;
        PreparedStatement selectStmt = null;
        Daily daily = new Daily();

        try {
            connection = DriverManager.getConnection(URL, USERNAME, SQL_PASSWORD);

            String selectSql = "SELECT * FROM HAUTmain." + id + "_daily WHERE Day = ?";
            selectStmt = connection.prepareStatement(selectSql);
            selectStmt.setString(1, day); // day 값을 바인딩합니다.

            // SQL 쿼리 실행
            ResultSet resultSet = selectStmt.executeQuery();

            while (resultSet.next()) {
                daily.setID(resultSet.getInt("ID"));
                daily.setDay(resultSet.getString("Day"));
                daily.setText(resultSet.getString("Text"));

            }
            return daily;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public CompletableFuture<String> getChatAI(int id, String text, String day) {
        Connection connection;
        PreparedStatement selectStmt = null;
        List<List<String>> chatHistory = new ArrayList<>();
        List<String> conversation = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(URL, USERNAME, SQL_PASSWORD);

            String selectSql = "SELECT * FROM HAUTmain." + id + "_chat WHERE ChatDate = ?";
            selectStmt = connection.prepareStatement(selectSql);
            selectStmt.setString(1, day); // day 값을 바인딩합니다.

            // SQL 쿼리 실행
            ResultSet resultSet = selectStmt.executeQuery();

            while (resultSet.next()) {
                String message = resultSet.getString("Message");
                int who = resultSet.getInt("Who");
                conversation.add(message);

                if (who == 0) {
                    // 사용자 대화 뒤에 AI 대화가 오면 대화 기록을 추가하고 초기화
                    chatHistory.add(conversation);
                    conversation = new ArrayList<>();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // HttpClient 생성
        HttpClient httpClient = HttpClient.newBuilder().build();

        // HTTP POST 요청을 보낼 URL 지정
        String url = AIURL + "/gen";

        JSONObject requestinput = new JSONObject();

        requestinput.put("message", text);
        requestinput.put("history", chatHistory);

        String jsonBody = requestinput.toString();

        // HTTP Request BodyPublisher 생성
        BodyPublisher requestBody = BodyPublishers.ofString(jsonBody);

        // HTTP POST Request 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(requestBody)
                .build();

        // 비동기적으로 HTTP POST 요청 보내기
        CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());

        // CompletableFuture를 반환
        return responseFuture.thenApply(response -> {
            try {
                String responseBody = response.body();

                // "text" 필드의 값을 추출하기 위한 정규 표현식
                String pattern = "\"text\":\"(.*?)\"";
                Pattern r = Pattern.compile(pattern);

                Matcher m = r.matcher(responseBody);

                String textValue = "";
                if (m.find()) {
                    // 정규 표현식으로 "text" 필드의 값을 추출
                    textValue = m.group(1);
                    // 백슬래시로 이스케이핑된 문자열을 UTF-8로 디코딩
                    textValue = unescapeUnicode(textValue);
                    saveChatHistory(id, day, text, textValue);

                    // AI 응답을 반환
                    return textValue;
                } else {
                    // "text" 필드를 찾지 못한 경우 예외 처리
                    throw new RuntimeException("Response does not contain 'text' field");
                }
            } catch (Exception e) {
                // 예외 발생 시 saveChatHistory 호출 후 "생성에러" 반환
                saveChatHistory(id, day, text, "생성에러 400");
                throw new RuntimeException("Error processing response", e);
            }
        }).exceptionally(e -> {
            // CompletableFuture 내부에서 발생한 예외 처리
            saveChatHistory(id, day, text, "생성에러");
            return "생성에러";
        });

    }

    public static String unescapeUnicode(String input) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char currentChar = input.charAt(i);
            if (currentChar == '\\' && i + 1 < input.length() && input.charAt(i + 1) == 'u') {
                // 유니코드 이스케이프 문자열 발견
                String unicodeHex = input.substring(i + 2, i + 6);
                int codePoint = Integer.parseInt(unicodeHex, 16);
                builder.appendCodePoint(codePoint);
                i += 6;
            } else {
                // 다른 문자는 그대로 추가
                builder.append(currentChar);
                i++;
            }
        }
        return builder.toString();
        // return builder.toString().replace("\\n", "<br>");
    }

    public void saveChatHistory(int id, String day, String userText, String AiText) {
        Connection connection;
        PreparedStatement insertStmt = null;

        try {
            connection = DriverManager.getConnection(URL, USERNAME, SQL_PASSWORD);

            // 유저내용 추가
            String insertSql = "INSERT INTO HAUTmain." + id + "_chat (Message, ChatDate, Who) VALUES (?, ?, ?)";
            insertStmt = connection.prepareStatement(insertSql);
            insertStmt.setString(1, userText);
            insertStmt.setString(2, day);
            insertStmt.setBoolean(3, true);
            // SQL 쿼리 실행
            insertStmt.executeUpdate();

            String aiInsertSql = "INSERT INTO HAUTmain." + id + "_chat (Message, ChatDate, Who) VALUES (?, ?, ?)";
            insertStmt = connection.prepareStatement(aiInsertSql);
            insertStmt.setString(1, AiText);
            insertStmt.setString(2, day);
            insertStmt.setBoolean(3, false);
            // SQL 쿼리 실행
            insertStmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Boolean makeDaily(String id, String day, String userId) {
        Connection connection;
        PreparedStatement selectStmt = null;
        List<String> conversation = new ArrayList<>();
        List<List<String>> chatHistory = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(URL, USERNAME, SQL_PASSWORD);

            String selectSql = "SELECT * FROM HAUTmain." + id + "_chat WHERE ChatDate = ?";
            selectStmt = connection.prepareStatement(selectSql);
            selectStmt.setString(1, day); // day 값을 바인딩합니다.

            // SQL 쿼리 실행
            ResultSet resultSet = selectStmt.executeQuery();

            while (resultSet.next()) {
                String message = resultSet.getString("Message");
                int who = resultSet.getInt("Who");
                conversation.add(message);

                if (who == 1) {
                    // 사용자 대화 뒤에 AI 대화가 오면 대화 기록을 추가하고 초기화
                    chatHistory.add(conversation);
                    conversation = new ArrayList<>();
                }
            }

            // HttpClient 생성
            HttpClient httpClient = HttpClient.newBuilder().build();

            // HTTP POST 요청을 보낼 URL 지정
            String url = AIURL + "/makeDaily";

            // JSON 형식의 요청 바디 데이터 생성
            JSONObject requestinput = new JSONObject();

            requestinput.put("history", chatHistory);

            String jsonBody = requestinput.toString();

            // HTTP Request BodyPublisher 생성
            BodyPublisher requestBody = BodyPublishers.ofString(jsonBody);

            // HTTP POST Request 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(requestBody)
                    .build();

            // 비동기적으로 HTTP POST 요청 보내기
            CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request,
                    HttpResponse.BodyHandlers.ofString());

            // CompletableFuture를 반환
            return responseFuture.thenApply(response -> {
                String responseBody = response.body();

                JSONObject jsonResponse = new JSONObject(responseBody);

                String t5Text = jsonResponse.getString("T5_text");
                String openaiText = jsonResponse.getString("OPENAI_text");

                // // "text" 필드의 값을 추출하기 위한 정규 표현식
                // String pattern = "\"text\":\"(.*?)\"";
                // Pattern r = Pattern.compile(pattern);

                // Matcher m = r.matcher(responseBody);
                // if (m.find()) {
                // // 정규 표현식으로 "text" 필드의 값을 추출
                // textValue = m.group(1);
                // // 백슬래시로 이스케이핑된 문자열을 UTF-8로 디코딩
                // textValue = unescapeUnicode(textValue);
                // }

                String textValue = null;

                t5Text = unescapeUnicode(t5Text);
                openaiText = unescapeUnicode(openaiText);

                textValue = "T5 요약\n\n" + t5Text + "\n\nOPENAI 요약\n\n" + openaiText;

                if (t5Text == null && openaiText == null)
                    return false;

                try {
                    String insertSql = "INSERT INTO HAUTmain." + id + "_daily (Day, Text) VALUES (?, ?)";
                    PreparedStatement insertStmt = connection.prepareStatement(insertSql);
                    insertStmt.setString(1, day);
                    insertStmt.setString(2, textValue);
                    insertStmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }

                // true 반환
                return true;
            }).join(); // CompletableFuture를 동기적으로 실행하고 결과를 기다림

        } catch (Exception e) {
            e.printStackTrace();
            return false; // 예외가 발생하면 false 반환
        }
    }

    public boolean removeDaily(int id, String day) {
        Connection connection = null;
        PreparedStatement deleteStmt = null;

        try {
            connection = DriverManager.getConnection(URL, USERNAME, SQL_PASSWORD);

            // HAUTmain." + id + "_chat 테이블에서 해당 날짜의 데이터 삭제
            String deleteChatSql = "DELETE FROM HAUTmain." + id + "_chat WHERE ChatDate = ?";
            deleteStmt = connection.prepareStatement(deleteChatSql);
            deleteStmt.setString(1, day);
            // SQL DELETE 쿼리 실행
            deleteStmt.executeUpdate();

            if (deleteStmt != null) {
                deleteStmt.close();
            }

            // HAUTmain." + id + "_daily 테이블에서 해당 날짜의 데이터 삭제
            String deleteDailySql = "DELETE FROM HAUTmain." + id + "_daily WHERE Day = ?";
            deleteStmt = connection.prepareStatement(deleteDailySql);
            deleteStmt.setString(1, day);
            // SQL DELETE 쿼리 실행
            deleteStmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (deleteStmt != null) {
                    deleteStmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

}
