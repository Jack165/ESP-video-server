package com.feng.videoserver.websocket;

import com.feng.videoserver.service.impl.VideoStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 全局Session管理器
 */

public class VideoStreamWebSocketHandler extends AbstractWebSocketHandler {
    private static final String STORAGE_PATH = "./captures/";
    @Autowired
    private VideoStreamService videoStreamService;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        
        // 如果这是第一个连接，启动帧发送任务
        if (sessions.size() == 1) {
            startFrameSendingTask();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        
        // 如果没有连接了，停止帧发送任务
        if (sessions.isEmpty()) {
            scheduler.shutdownNow();
        }
    }

    /**
     * 启动帧发送任务
     */
    private void startFrameSendingTask() {
        scheduler.scheduleAtFixedRate(() -> {
            byte[] frame = videoStreamService.getNextFrame();
            if (frame != null && frame.length > 0) {
                sendFrameToAllSessions(frame);
            }
        }, 0, 33, TimeUnit.MILLISECONDS); // 约30fps
    }

    /**
     * 向所有会话发送帧
     * @param frame 帧数据
     */
    private void sendFrameToAllSessions(byte[] frame) {
        sessions.forEach((id, session) -> {
            if (session.isOpen()) {
                try {
                    BinaryMessage message = new BinaryMessage(frame);
                    session.sendMessage(message);
                } catch (IOException e) {
                    handleSessionError(id, session, e);
                }
            } else {
                // 如果 session 不再打开，则主动清理
                sessions.remove(id);
            }
        });
    }

    private void handleSessionError(String id, WebSocketSession session, IOException e) {
        // 使用日志框架替代 printStackTrace
        // logger.error("向会话发送消息失败: {}", id, e);
        sessions.remove(id); // 出现异常时移除失效会话
    }
}