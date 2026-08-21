package com.romualdo.rag_alura.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestPdf(MultipartFile file, String category, String author) throws IOException {
        Resource pdfResource = file.getResource();

        // 1. Extração robusta com Apache Tika
        TikaDocumentReader reader = new TikaDocumentReader(pdfResource);
        List<Document> rawDocuments = reader.get();

        // 2. Divisão em chunks
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> splitDocuments = splitter.apply(rawDocuments);

        // 3. Metadados
        splitDocuments.forEach(doc -> {
            Map<String, Object> metadata = doc.getMetadata();
            metadata.put("category", category != null ? category.toUpperCase() : "GERAL");
            metadata.put("author", author != null ? author : "Sistema");
            metadata.put("source", file.getOriginalFilename());
        });

        // 4. Armazenamento vetorial
        vectorStore.add(splitDocuments);
    }
}