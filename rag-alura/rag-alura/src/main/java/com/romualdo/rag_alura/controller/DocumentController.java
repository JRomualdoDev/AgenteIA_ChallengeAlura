package com.romualdo.rag_alura.controller;

import com.romualdo.rag_alura.service.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "GERAL") String category,
            @RequestParam(value = "author", defaultValue = "Admin") String author) {
        try {
            ingestionService.ingestPdf(file, category, author);
            return ResponseEntity.ok(Map.of("message", "Documento processado e indexado com sucesso!"));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Falha ao processar o arquivo: " + e.getMessage()));
        }
    }
}