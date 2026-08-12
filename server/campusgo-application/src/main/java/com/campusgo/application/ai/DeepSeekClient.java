package com.campusgo.application.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class DeepSeekClient {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(List<Message> messages) {
        try {
            ChatRequest request = new ChatRequest(model, messages, 0.7);
            String requestBody = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("DeepSeek API error: {} - {}", response.statusCode(), response.body());
                return "抱歉，AI 服务暂时不可用，请稍后再试。";
            }

            ChatResponse chatResponse = objectMapper.readValue(response.body(), ChatResponse.class);
            if (chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
                return chatResponse.getChoices().get(0).getMessage().getContent();
            }

            return "抱歉，我无法理解您的问题，请重新描述。";

        } catch (Exception e) {
            log.error("DeepSeek API call failed", e);
            return "抱歉，AI 服务调用失败，请稍后再试。";
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ChatRequest {
        private String model;
        private List<Message> messages;
        private double temperature;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatResponse {
        private List<Choice> choices;

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Choice {
            private int index;
            private Message message;
            private String finish_reason;
        }
    }
}
