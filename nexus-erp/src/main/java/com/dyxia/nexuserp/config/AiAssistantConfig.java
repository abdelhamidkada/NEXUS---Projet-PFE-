package com.dyxia.nexuserp.config;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de l'assistant IA LangChain4j avec le modèle Google Gemini.
 */
@Configuration
public class AiAssistantConfig {

    /**
     * Déclaration du modèle de langage Google Gemini comme un Bean Spring.
     * Utilise la clé API spécifiée dans application.yml (nexus.ai.gemini.api-key).
     */
    @Bean
    public GoogleAiGeminiChatModel googleAiGeminiChatModel(@Value("${nexus.ai.gemini.api-key}") String apiKey) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .build();
    }
}
