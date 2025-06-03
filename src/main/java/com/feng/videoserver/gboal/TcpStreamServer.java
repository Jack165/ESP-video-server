package com.feng.videoserver.gboal;

import com.feng.videoserver.Socket.ClientHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Socket服务锻，监听8004端口，当有客户端连接时，创建一个线程处理客户端请求
 */
@Component
public class TcpStreamServer {
    private static final int STREAM_PORT = 8004;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    @Autowired
    private GlobalImageCache imageCacheService;

    public void start() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(STREAM_PORT)) {
                while (true) {
                    Socket client = server.accept();
                    threadPool.execute(new ClientHandler(client,imageCacheService));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}