package org.example.sirnaq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TaskWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(TaskWebSocketHandler.class);
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        logger.info("WebSocket session connected: {}", session.getId());
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        logger.info("WebSocket session closed: {} (status: {})", session.getId(), status);
    }

    public void sendTaskUpdate(Long taskId) throws Exception {
        String message = "{\"event\": \"taskUpdated\", \"id\":" + taskId + "}";
        logger.info("Sending WebSocket update: {}", message);
        if (sessions.isEmpty()) {
            logger.warn("No WebSocket sessions connected");
        }
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            } else {
                logger.warn("Session {} is closed", session.getId());
            }
        }
        logger.info("WebSocket update sent to {} sessions", sessions.size());
    }

}
