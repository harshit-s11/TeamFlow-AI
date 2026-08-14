package com.teamflow.backend.infrastructure.ai;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.teamflow.backend.common.exception.AiGatewayTimeoutException;
import com.teamflow.backend.common.exception.AiServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiApiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiClient.class);

    private final String apiKey;
    private final String model;
    private final int timeoutMs;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiApiClient(
            @Value("${application.ai.gemini.api-key:}") String apiKey,
            @Value("${application.ai.gemini.model:gemini-3.6-flash}") String model,
            @Value("${application.ai.gemini.timeout-ms:12000}") int timeoutMs,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutMs = timeoutMs;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    public String generateContent(String systemInstruction, String prompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new AiServiceUnavailableException("AI service is not configured");
        }

        String endpointUrl = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                model, apiKey
        );

        Map<String, Object> requestBodyMap = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemInstruction))
                ),
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "response_mime_type", "application/json"
                )
        );

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(requestBodyMap);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize Gemini request payload", e);
        }

        int maxAttempts = 2; // 1 initial + 1 retry
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpointUrl))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofMillis(timeoutMs))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();

                if (statusCode == 200) {
                    return parseTextFromGeminiResponse(response.body());
                } else if ((statusCode == 503 || statusCode == 429) && attempt < maxAttempts) {
                    log.warn("Gemini API transient failure (HTTP {}), retrying in 1s...", statusCode);
                    Thread.sleep(1000);
                    continue;
                } else if (statusCode == 503 || statusCode == 429) {
                    throw new AiServiceUnavailableException("Gemini AI service unavailable (HTTP " + statusCode + ")");
                } else {
                    log.error("Gemini API error (HTTP {}): {}", statusCode, response.body());
                    throw new AiServiceUnavailableException("Gemini AI service error (HTTP " + statusCode + ")");
                }
            } catch (java.net.http.HttpTimeoutException e) {
                if (attempt < maxAttempts) {
                    log.warn("Gemini API timeout, retrying...");
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    continue;
                }
                throw new AiGatewayTimeoutException("AI service request timed out after " + timeoutMs + "ms");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AiServiceUnavailableException("Gemini API execution interrupted");
            } catch (AiServiceUnavailableException | AiGatewayTimeoutException e) {
                throw e;
            } catch (Exception e) {
                log.error("Unexpected error invoking Gemini API", e);
                throw new AiServiceUnavailableException("Failed to communicate with AI service: " + e.getMessage());
            }
        }

        throw new AiServiceUnavailableException("AI service request failed after retries");
    }

    private String parseTextFromGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
            throw new AiServiceUnavailableException("Invalid response structure from Gemini API");
        } catch (Exception e) {
            log.error("Failed to parse Gemini API response: {}", responseBody, e);
            throw new AiServiceUnavailableException("Failed to parse AI model response");
        }
    }
}
