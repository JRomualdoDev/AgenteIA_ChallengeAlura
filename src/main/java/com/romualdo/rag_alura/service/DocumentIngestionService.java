package com.romualdo.rag_alura.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);
    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestPdf(MultipartFile file, String category, String author) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "documento.pdf";
        byte[] fileBytes = file.getBytes();

        Resource pdfResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        // 1. Extração robusta com Apache Tika
        log.info("[1/4] Extraindo texto do PDF '{}' com Apache Tika...", filename);
        TikaDocumentReader reader = new TikaDocumentReader(pdfResource);
        List<Document> rawDocuments = reader.get();

        if (rawDocuments.isEmpty()) {
            throw new IllegalArgumentException("Nenhum texto pôde ser extraído do documento PDF.");
        }
        log.info("[1/4] Texto extraído com sucesso ({} documento(s) bruto(s)).", rawDocuments.size());

        // 2. Divisão em chunks
        log.info("[2/4] Dividindo texto em chunks semânticos com TokenTextSplitter...");
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> splitDocuments = splitter.apply(rawDocuments);
        log.info("[2/4] Divisão concluída: gerados {} chunks.", splitDocuments.size());

        // 3. Metadados
        log.info("[3/4] Aplicando metadados aos chunks...");
        splitDocuments.forEach(doc -> {
            Map<String, Object> metadata = doc.getMetadata();
            metadata.put("category", category != null ? category.toUpperCase() : "GERAL");
            metadata.put("author", author != null ? author : "Sistema");
            metadata.put("source", filename);
        });

        // 4. Armazenamento vetorial (Embeddings + PGVector)
        log.info("[4/4] Gerando embeddings locais (ONNX) e salvando no PGVector (isso pode levar alguns segundos)...");
        vectorStore.add(splitDocuments);
        log.info("[4/4] Ingestão vetorial concluída com sucesso para '{}'!", filename);
    }
}