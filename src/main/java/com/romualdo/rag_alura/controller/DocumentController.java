package com.romualdo.rag_alura.controller;

import com.romualdo.rag_alura.service.DocumentIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentIngestionService ingestionService;

    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "GERAL") String category,
            @RequestParam(value = "author", defaultValue = "Admin") String author) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "O arquivo enviado está vazio."));
        }

        try {
            log.info("Recebida solicitação de upload: arquivo='{}', categoria='{}', autor='{}', tamanho={} bytes",
                    file.getOriginalFilename(), category, author, file.getSize());

            ingestionService.ingestPdf(file, category, author);

            log.info("Documento '{}' processado e indexado com sucesso!", file.getOriginalFilename());
            return ResponseEntity.ok(Map.of("message", "Documento processado e indexado com sucesso!"));
        } catch (Exception e) {
            log.error("Falha ao processar e indexar o arquivo '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Falha ao processar o arquivo: " + e.getMessage()));
        }
    }
}