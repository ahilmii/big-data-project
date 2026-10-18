package org.ahilmi.schoolmanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.dto.ChatRequest;
import org.ahilmi.schoolmanagement.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(chatService.chat(request));
    }
    public record ChatResponse(String response) {}
}
