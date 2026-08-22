package com.romualdo.rag_alura.service;

import com.romualdo.rag_alura.dto.ChatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagChatService {

    private static final Logger log = LoggerFactory.getLogger(RagChatService.class);
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private static final String RAG_PROMPT_TEMPLATE = """
            Você é o Assistente Virtual Corporativo oficial da empresa.
            Sua missão é responder à dúvida do colaborador usando ESTRITAMENTE as informações dos trechos de documentos fornecidos no contexto.
            
            REGRAS OBRIGATÓRIAS:
            1. Responda apenas com base no CONTEXTO RECUPERADO. Não invente ou presuma nada fora dele.
            2. Se a informação não estiver presente ou for insuficiente, responda exatamente: "Não encontrei essa informação nos documentos oficiais disponíveis. Por favor, entre em contato com a área responsável (RH, Financeiro ou Jurídico)."
            3. Seja formal, direto e objetivo.
            
            --- CONTEXTO RECUPERADO ---
            {context}
            
            --- PERGUNTA DO COLABORADOR ---
            {question}
            """;

    public RagChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public ChatResponseDTO answerQuestion(String question, String categoryFilter) {
        log.info("Processando pergunta no RAG: '{}' (Categoria: {})", question, categoryFilter);

        // 1. Busca Semântica no PostgreSQL Vector
        SearchRequest searchRequest = SearchRequest.query(question)
                .withTopK(4)
                .withSimilarityThreshold(0.0);

        List<Document> retrievedDocs = vectorStore.similaritySearch(searchRequest);
        log.info("Busca semântica concluída: {} trecho(s) relevante(s) encontrado(s).", retrievedDocs.size());

        // Caso nenhum documento relevante seja encontrado
        if (retrievedDocs.isEmpty()) {
            log.warn("Nenhum trecho de documento foi recuperado do PGVector para a pergunta: '{}'", question);
            return new ChatResponseDTO(
                    "Não encontrei documentos oficiais com relevância para sua dúvida. Por favor, entre em contato com o suporte ou a área responsável.",
                    List.of()
            );
        }

        // 2. Extrai as fontes únicas para citação
        List<String> sources = retrievedDocs.stream()
                .map(doc -> String.format("%s (Pág. %s)",
                        doc.getMetadata().getOrDefault("source", "Documento"),
                        doc.getMetadata().getOrDefault("page_number", "N/A")))
                .distinct()
                .toList();

        // 3. Monta o bloco de contexto
        String context = retrievedDocs.stream()
                .<String>map(doc -> String.format("[Origem: %s | Pág: %s]\n%s",
                        doc.getMetadata().getOrDefault("source", "Desconhecido"),
                        doc.getMetadata().getOrDefault("page_number", "N/A"),
                        doc.getContent()))
                .collect(Collectors.joining("\n\n"));

        // 4. Executa a geração com LLM (Groq)
        log.info("Enviando prompt com contexto para o LLM (Groq)...");
        PromptTemplate template = new PromptTemplate(RAG_PROMPT_TEMPLATE);
        Prompt prompt = template.create(Map.of(
                "context", context,
                "question", question
        ));

        String answer = chatClient.prompt(prompt).call().content();
        log.info("Resposta gerada com sucesso pelo LLM!");

        return new ChatResponseDTO(answer, sources);
    }
}