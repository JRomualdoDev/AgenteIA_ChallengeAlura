package com.romualdo.rag_alura.dto;

import java.util.List;

public record ChatResponseDTO(
        String answer,
        List<String> sources
) {}