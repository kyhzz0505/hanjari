package com.hanjari.backend.ai;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.json.JsonMapper;

// Google Gemini API를 호출하는 곳. AI 호출은 모두 이 클래스를 거친다.
// 키는 application.yaml → 환경변수 GEMINI_API_KEY에서 받고, 코드·로그에는 남기지 않는다.
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String thinkingLevel;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public GeminiClient(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model,
            @Value("${gemini.timeout-seconds}") int timeoutSeconds,
            @Value("${gemini.thinking-level:}") String thinkingLevel) {
        this.apiKey = apiKey;
        this.model = model;
        this.thinkingLevel = thinkingLevel;

        // 응답이 늦으면 오래 기다리지 않고 실패로 처리한다 (실패하면 일반 검색 결과만 보낸다)
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .requestFactory(requestFactory)
                .build();
    }

    // 자유 문장으로 답을 받는다 (추천 설명용)
    public String generateText(String prompt) {
        return generate(prompt, Map.of("temperature", 0.7));
    }

    // 정해진 JSON 형식(schema)으로 답을 받아 자바 객체로 바꾼다 (조건 추출용)
    public <T> T generateJson(String prompt, Map<String, Object> schema, Class<T> type) {
        String json = generate(prompt, Map.of(
                "temperature", 0,
                "responseMimeType", "application/json",
                "responseSchema", schema));
        return jsonMapper.readValue(json, type);
    }

    private String generate(String prompt, Map<String, Object> generationConfig) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY가 설정되지 않았습니다.");
        }
        // 일부 모델은 답하기 전에 오래 "생각"한다. 설정(gemini.thinking-level)이 있을 때만 생각 수준을 정한다
        Map<String, Object> config = new HashMap<>(generationConfig);
        if (thinkingLevel != null && !thinkingLevel.isBlank()) {
            config.put("thinkingConfig", Map.of("thinkingLevel", thinkingLevel));
        }
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", config);

        // Gemini가 Content-Type을 application/octet-stream으로 보내는 경우가 있어서
        // 본문을 글자 그대로 받은 뒤 직접 JSON으로 읽는다
        String raw = restClient.post()
                .uri("/models/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        GeminiResponse response = raw == null ? null : jsonMapper.readValue(raw, GeminiResponse.class);

        if (response == null || response.candidates() == null || response.candidates().isEmpty()
                || response.candidates().get(0).content() == null
                || response.candidates().get(0).content().parts() == null
                || response.candidates().get(0).content().parts().isEmpty()) {
            throw new IllegalStateException("Gemini 응답이 비어 있습니다.");
        }
        String text = response.candidates().get(0).content().parts().get(0).text();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Gemini 응답이 비어 있습니다.");
        }
        return text.trim();
    }

    // Gemini 응답 JSON 중 필요한 부분만 받는다
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeminiResponse(List<Candidate> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(Content content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(List<Part> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Part(String text) {
    }
}
