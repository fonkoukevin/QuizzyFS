package com.quizzy.quizzy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizzy.quizzy.entity.Quiz;
import com.quizzy.quizzy.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandlerImpl extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandlerImpl.class);
    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Stockage des sessions WebSocket actives par executionId
    private static final Map<String, Set<WebSocketSession>> activeSessions = new ConcurrentHashMap<>();

    // Stockage de l'hôte de chaque executionId
    private static final Map<String, WebSocketSession> executionHost = new ConcurrentHashMap<>();

    public WebSocketHandlerImpl(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOGGER.info("✅ Connexion WebSocket ouverte : " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        LOGGER.info("📩 Message reçu : " + message.getPayload());

        // Parse JSON
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());
        String eventName = jsonNode.get("name").asText();
        String executionId = jsonNode.get("data").get("executionId").asText();

        if ("host".equals(eventName)) {
            handleHostJoin(session, executionId);
        } else if ("join".equals(eventName)) {
            handleParticipantJoin(session, executionId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOGGER.info("❌ Connexion WebSocket fermée : " + session.getId());
        removeParticipant(session);
    }

    private void handleHostJoin(WebSocketSession session, String executionId) {
        LOGGER.info("📌 Host rejoint la session " + executionId);

        // Vérifier si l'exécution existe
        Quiz quiz = quizRepository.findByExecutionId(executionId).orElse(null);
        if (quiz == null) {
            sendMessage(session, "{\"error\": \"Execution not found\"}");
            return;
        }

        // Stocke l'hôte
        executionHost.put(executionId, session);

        // Ajouter l'hôte à l'exécution
        activeSessions.computeIfAbsent(executionId, k -> ConcurrentHashMap.newKeySet()).add(session);

        // Envoyer les détails du quiz à l'hôte
        sendMessage(session, String.format(
                "{\"name\": \"hostDetails\", \"data\": {\"quiz\": {\"title\": \"%s\"}}}",
                quiz.getTitle()
        ));

        // Envoyer le statut aux participants
        broadcastStatus(executionId);
    }

    private void handleParticipantJoin(WebSocketSession session, String executionId) {
        LOGGER.info("👤 Participant rejoint la session " + executionId);

        // Vérifier si l'exécution existe
        Quiz quiz = quizRepository.findByExecutionId(executionId).orElse(null);
        if (quiz == null) {
            sendMessage(session, "{\"error\": \"Execution not found\"}");
            return;
        }

        // Ajouter le participant à l'exécution
        activeSessions.computeIfAbsent(executionId, k -> ConcurrentHashMap.newKeySet()).add(session);

        // Envoyer les détails du quiz au participant
        sendMessage(session, String.format(
                "{\"name\": \"joinDetails\", \"data\": {\"quizTitle\": \"%s\"}}",
                quiz.getTitle()
        ));

        // Diffuser le statut mis à jour à tous les participants
        broadcastStatus(executionId);
    }

    private void removeParticipant(WebSocketSession session) {
        activeSessions.forEach((executionId, sessions) -> {
            if (sessions.remove(session)) {
                LOGGER.info("👤 Participant déconnecté de la session " + executionId);
                broadcastStatus(executionId);
            }
        });
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            LOGGER.error("Erreur envoi message WebSocket", e);
        }
    }

    private void broadcastStatus(String executionId) {
        Set<WebSocketSession> sessions = activeSessions.get(executionId);
        if (sessions == null) return;

        String statusMessage = String.format(
                "{\"name\": \"status\", \"data\": {\"status\": \"waiting\", \"participants\": %d}}",
                sessions.size()
        );

        for (WebSocketSession session : sessions) {
            sendMessage(session, statusMessage);
        }
    }
}
