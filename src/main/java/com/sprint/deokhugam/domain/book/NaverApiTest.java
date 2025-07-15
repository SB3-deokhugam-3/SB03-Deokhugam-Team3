package com.sprint.deokhugam.domain.book;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * NaverApiTest 클래스는 Naver Open API(Book Advanced Search)를 호출하여
 * ISBN을 기반으로 도서 정보를 조회하는 간단한 예제 프로그램입니다.
 *
 * - .env 파일에서 NAVER_CLIENT_ID와 NAVER_CLIENT_SECRET을 불러와 인증에 사용합니다.
 * - HTTP GET 요청을 통해 JSON 형식의 결과를 받아 출력합니다.
 *
 * @author
 */
public class NaverApiTest {

    private final String clientId;
    private final String clientSecret;

    /**
     * 생성자.
     * .env 파일에서 NAVER_CLIENT_ID, NAVER_CLIENT_SECRET을 불러와 초기화합니다.
     *
     * @throws IOException .env 파일을 읽지 못할 경우 발생
     */
    public NaverApiTest() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(".env"));

        clientId = properties.getProperty("NAVER_CLIENT_ID");
        clientSecret = properties.getProperty("NAVER_CLIENT_SECRET");
    }

    /**
     * 지정된 URL로 HTTP GET 요청을 보내고 결과를 문자열로 반환합니다.
     *
     * @param apiUrl 요청을 보낼 URL
     * @param requestHeaders 요청 헤더 (X-Naver-Client-Id, X-Naver-Client-Secret 등)
     * @return API 응답 본문(문자열)
     * @throws RuntimeException 요청 실패시 발생
     */
    private static String get(String apiUrl, Map<String, String> requestHeaders) {
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readBody(con.getInputStream());
            } else {
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    /**
     * 주어진 URL 문자열로 HttpURLConnection 객체를 생성합니다.
     *
     * @param apiUrl 연결할 URL
     * @return HttpURLConnection 객체
     * @throws RuntimeException URL 형식 오류 또는 연결 실패시 발생
     */
    private static HttpURLConnection connect(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    /**
     * InputStream을 읽어 문자열로 반환합니다.
     *
     * @param body InputStream (API 응답)
     * @return 문자열로 변환된 응답 본문
     * @throws RuntimeException 읽기 실패시 발생
     */
    private static String readBody(InputStream body) {
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }

    /**
     * 프로그램 실행 진입점.
     * ISBN을 기준으로 도서 검색 API를 호출하여 결과를 출력합니다.
     *
     * @param args 커맨드라인 인자(사용하지 않음)
     * @throws IOException .env 파일 읽기 실패시 발생
     */
    public static void main(String[] args) throws IOException {
        NaverApiTest test = new NaverApiTest();

        String isbn = "9788970509471";
        String apiURL = "https://openapi.naver.com/v1/search/book_adv.json?d_isbn=" + isbn;

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", test.clientId);
        requestHeaders.put("X-Naver-Client-Secret", test.clientSecret);

        String responseBody = get(apiURL, requestHeaders);

        System.out.println(responseBody);
    }
}
