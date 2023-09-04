package com.howareyoutoday.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KaKaoService {
    @Value("${spring.datasource.url}")
    String URL;

    @Value("${spring.datasource.username}")
    String USERNAME;

    @Value("${spring.datasource.password}")
    String SQL_PASSWORD;

    @Value("${kakao.clientId}")
    String client_id;

    @Value("${kakao.redirectUri}")
    String redirect_uri;

    public String getToken(String code) throws IOException {
        // 인가코드로 토큰받기
        String host = "https://kauth.kakao.com/oauth/token";
        URL url = new URL(host);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String token = "";

        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true); // 데이터 기록 알려주기

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=" + client_id);
            sb.append("&redirect_uri=" + redirect_uri);
            sb.append("&code=" + code);

            bw.write(sb.toString());
            bw.flush();

            int responseCode = urlConnection.getResponseCode();
            System.out.println("responseCode = " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = "";
            String result = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("result = " + result);

            // json parsing
            JSONParser parser = new JSONParser();
            JSONObject elem = (JSONObject) parser.parse(result);

            String access_token = elem.get("access_token").toString();
            String refresh_token = elem.get("refresh_token").toString();
            System.out.println("refresh_token = " + refresh_token);
            System.out.println("access_token = " + access_token);

            token = access_token;

            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return token;
    }

    public Map<String, String> getUserInfo(String access_token) throws IOException {
        String host = "https://kapi.kakao.com/v2/user/me";
        Map<String, String> result = new HashMap<>();
        try {
            URL url = new URL(host);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + access_token);
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            System.out.println("responseCode = " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = "";
            String res = "";
            while ((line = br.readLine()) != null) {
                res += line;
            }

            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(res);
            JSONObject kakao_account = (JSONObject) obj.get("kakao_account");
            JSONObject properties = (JSONObject) obj.get("properties");

            String id = obj.get("id").toString();
            String nickname = properties.get("nickname").toString();
            String image = properties.get("profile_image").toString();
            String email = kakao_account.get("email").toString();

            String age_range = kakao_account.get("age_range") != null ? kakao_account.get("age_range").toString()
                    : "";
            String gender = kakao_account.get("gender") != null ? kakao_account.get("gender").toString() : "";

            result.put("id", id);
            result.put("nickname", nickname);
            result.put("image", image);
            result.put("email", email);
            result.put("gender", gender);
            result.put("age_range", age_range);

            br.close();

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String getAgreementInfo(String access_token) {
        String result = "";
        String host = "https://kapi.kakao.com/v2/user/scopes";
        try {
            URL url = new URL(host);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "Bearer " + access_token);

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }

            int responseCode = urlConnection.getResponseCode();
            System.out.println("responseCode = " + responseCode);

            // result is json format
            br.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Integer emailCheck(Map<String, String> userInfo, String userCookie) {
        Connection connection;
        PreparedStatement selectStmt = null;

        try {
            connection = DriverManager.getConnection(URL, USERNAME, SQL_PASSWORD);

            // SQL 쿼리 준비
            String selectSql = "SELECT ID FROM HAUTmain.Member WHERE Email = ?";
            selectStmt = connection.prepareStatement(selectSql);
            selectStmt.setString(1, userInfo.get("email"));

            // SQL 쿼리 실행
            ResultSet resultSet = selectStmt.executeQuery();

            if (resultSet.next()) {
                // 이미 존재하는 경우 해당 회원의 ID 반환
                Integer userId = resultSet.getInt("ID");
                // 토큰 업데이트를 위한 UPDATE 쿼리 실행
                String updateSql = "UPDATE HAUTmain.Member SET Token = ? WHERE ID = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                updateStmt.setString(1, userCookie);
                updateStmt.setInt(2, userId);

                int affectedRows = updateStmt.executeUpdate();

                if (affectedRows > 0) {
                    // 토큰 업데이트가 성공하면 해당 사용자의 ID를 반환합니다.
                    return userId;
                }
                return resultSet.getInt("ID");
            } else {
                // 존재하지 않는 경우 정보를 추가하고 ID 반환
                String insertSql = "INSERT INTO HAUTmain.Member (Email, Name, Age, Gender, ImgPath, Token) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, userInfo.get("email"));
                insertStmt.setString(2, userInfo.get("nickname"));
                insertStmt.setString(3, userInfo.get("age_range"));
                insertStmt.setString(4, userInfo.get("gender"));
                insertStmt.setString(5, userInfo.get("image"));
                insertStmt.setString(6, userCookie);

                int affectedRows = insertStmt.executeUpdate();

                if (affectedRows > 0) {
                    // 삽입 성공 시 생성된 ID 반환
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        String chatTableSql = "CREATE TABLE IF NOT EXISTS HAUTmain." + generatedKeys.getInt(1)
                                + "_chat ("
                                + "ID INT AUTO_INCREMENT PRIMARY KEY,"
                                + "Message TEXT,"
                                + "ChatDate DATE,"
                                + "Who BOOLEAN"
                                + ")";

                        String dailyTableSql = "CREATE TABLE IF NOT EXISTS HAUTmain." + generatedKeys.getInt(1)
                                + "_daily ("
                                + "ID INT AUTO_INCREMENT PRIMARY KEY,"
                                + "Day DATE,"
                                + "Text TEXT"
                                + ")";

                        Statement createTableStmt = connection.createStatement();
                        createTableStmt.executeUpdate(chatTableSql); // [id]_chat 테이블 생성
                        createTableStmt.executeUpdate(dailyTableSql); // [id]_daily 테이블 생성

                        return generatedKeys.getInt(1);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
