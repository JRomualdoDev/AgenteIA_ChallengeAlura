package com.romualdo.rag_alura.controller;

import com.romualdo.rag_alura.dto.ChatRequestDTO;
import com.romualdo.rag_alura.dto.ChatResponseDTO;
import com.romualdo.rag_alura.service.RagChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final RagChatService ragChatService;

    public ChatController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> ask(@RequestBody ChatRequestDTO request) {
        if (request == null || request.question() == null || request.question().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatResponseDTO("A pergunta não pode estar vazia.", List.of()));
        }

        try {
            ChatResponseDTO response = ragChatService.answerQuestion(request.question(), request.category());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao processar requisição no ChatController: {}", e.getMessage(), e);
            String errorMessage = "Falha ao gerar resposta: " + e.getMessage();
            if (e.getMessage() != null && (e.getMessage().contains("401") || e.getMessage().toLowerCase().contains("unauthorized") || e.getMessage().toLowerCase().contains("invalid api key"))) {
                errorMessage = "Erro de autenticação com o LLM (Groq): Verifique se a variável de ambiente GROQ_API_KEY foi adicionada nas configurações do Railway com uma chave válida do console.groq.com.";
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponseDTO(errorMessage, List.of()));
        }
    }
}