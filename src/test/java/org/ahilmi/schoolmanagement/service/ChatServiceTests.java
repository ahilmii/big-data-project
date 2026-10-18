package org.ahilmi.schoolmanagement.service;

import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ahilmi.schoolmanagement.dto.ChatRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class ChatServiceTests {
    @Test
    void sendsModelHistoryAndNonStreamingRequest() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> body = new AtomicReference<>();
        server.createContext("/api/chat", exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{\"message\":{\"role\":\"assistant\",\"content\":\"HDFS stores files.\"}}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            var service = new ChatService("http://127.0.0.1:" + server.getAddress().getPort(), "llama3.2:latest");
            assertEquals("HDFS stores files.", service.chat(new ChatRequest("Explain HDFS", List.of(new ChatRequest.Message("user", "Hello")))));
            var json = new ObjectMapper().readTree(body.get());
            assertEquals("llama3.2:latest", json.get("model").asText());
            assertFalse(json.get("stream").asBoolean());
            assertEquals(2, json.get("messages").size());
            assertEquals("Explain HDFS", json.get("messages").get(1).get("content").asText());
        } finally { server.stop(0); }
    }

    @Test
    void missingModelReturnsActionableError() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> { exchange.sendResponseHeaders(404, -1); exchange.close(); });
        server.start();
        try {
            var service = new ChatService("http://127.0.0.1:" + server.getAddress().getPort(), "llama3.2:latest");
            var error = assertThrows(ResponseStatusException.class, () -> service.chat(new ChatRequest("Hello", null)));
            assertEquals(503, error.getStatusCode().value());
            assertTrue(error.getReason().contains("ollama pull llama3.2:latest"));
        } finally { server.stop(0); }
    }
}
