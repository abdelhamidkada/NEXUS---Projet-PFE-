package com.dyxia.nexuserp.service;

import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Service d'intelligence artificielle pour l'assistant conversationnel (Chatbot) de NEXUS ERP.
 */
@AiService
@SystemMessage("You are an intelligent HR assistant for the NEXUS ERP platform. Be concise and helpful.")
public interface ChatBotService {

    /**
     * Envoie un message à l'assistant IA Gemini et retourne la réponse générée.
     *
     * @param userMessage Le message saisi par l'utilisateur.
     * @return La réponse textuelle de l'IA.
     */
    String chat(@UserMessage String userMessage);
}
