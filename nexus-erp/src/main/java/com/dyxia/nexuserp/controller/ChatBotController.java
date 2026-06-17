package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.service.ChatBotService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour le chatbot IA (Gemini).
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    /**
     * Endpoint POST pour interagir avec le Chatbot NEXUS ERP.
     * Accepte un payload JSON {"message": "..."}.
     * 
     * @param request Le DTO contenant le message de l'utilisateur.
     * @return La réponse générée par l'IA sous forme de chaîne de caractères.
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        String response = chatBotService.chat(request.getMessage());
        return ResponseEntity.ok(response);
    }

    /**
     * DTO représentant la requête de chat.
     */
    @Data
    public static class ChatRequest {
        private String message;
    }
}
