package com.romualdo.rag_alura.dto;

public record ChatRequestDTO(
        String question,
        String category
) {}