package com.romualdo.rag_alura.controller;

import com.romualdo.rag_alura.dto.ChatRequestDTO;
import com.romualdo.rag_alura.dto.ChatResponseDTO;
import com.romualdo.rag_alura.service.RagChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final RagChatService ragChatService;

    public ChatController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> ask(@RequestBody ChatRequestDTO request) {
        ChatResponseDTO response = ragChatService.answerQuestion(request.question(), request.category());
        return ResponseEntity.ok(response);
    }
}