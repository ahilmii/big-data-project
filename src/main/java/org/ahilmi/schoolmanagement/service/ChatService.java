package org.ahilmi.schoolmanagement.service;

import org.ahilmi.schoolmanagement.dto.ChatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    private final RestClient client;
    private final String model;

    public ChatService(@Value("${app.ollama.base-url:http://localhost:11434}") String baseUrl,
                       @Value("${app.ollama.model:llama3.2:latest}") String model) {
        this.model = model;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(180));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public String chat(ChatRequest request) {
        List<ChatRequest.Message> messages = new ArrayList<>();
        if (request.history() != null) messages.addAll(request.history());
        messages.add(new ChatRequest.Message("user", request.message()));
        try {
            OllamaResponse result = client.post().uri("/api/chat")
                    .body(new OllamaRequest(model, messages, false)).retrieve().body(OllamaResponse.class);
            if (result == null || result.message() == null || result.message().content() == null
                    || result.message().content().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ollama boş yanıt döndürdü.");
            }
            return result.message().content();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Ollama modeli bulunamadı. ollama pull " + model + " komutuyla modeli indirin.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ollama isteği tamamlanamadı.");
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Ollama yanıt vermiyor. ollama serve komutunu ve model kurulumunu kontrol edin.");
        }
    }
    private record OllamaRequest(String model, List<ChatRequest.Message> messages, boolean stream) {}
    private record OllamaResponse(ChatRequest.Message message) {}
}
