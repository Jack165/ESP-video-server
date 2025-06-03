package com.feng.videoserver.service.impl;

import com.feng.videoserver.Socket.ClientHandler;
import com.feng.videoserver.service.IVideoService;
import com.feng.videoserver.gboal.GlobalImageCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * MJPEG处理服务
 */
@Component
public class MJpegService implements IVideoService {

    private volatile boolean running = false;
    private final BlockingQueue<byte[]> frameQueue = new LinkedBlockingQueue<>(100);
    private Thread encodingThread;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static final int STREAM_PORT = 8004;
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
    /**
     * 启动编码线程
     */
    public void startEncodingThread() {
        running = true;
        encodingThread = new Thread(() -> {
            while (running) {
                try {
                    // 定期从缓存中获取最新图像并添加到流中
                    imageCacheService.getAllCachedImages().values().stream()
                            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                            .limit(1) // 只取最新的一张
                            .forEach(cachedImage -> addImageToStream(cachedImage.getImage()));

                    Thread.sleep(33); // 约30fps
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        encodingThread.setDaemon(true);
        encodingThread.start();
    }

    /**
     * 停止编码线程
     */
    public void stopEncodingThread() {
        running = false;
        if (encodingThread != null) {
            encodingThread.interrupt();
            try {
                encodingThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 添加图像到队列流
     * @param image 图像
     */
    public void addImageToStream(BufferedImage image) {
        if (running && image != null) {
            try {
                // 将图像编码为H.265帧并添加到队列
                //byte[] encodedFrame = encodeImageToH265(image);
                byte[] encodedFrame=convertToJpegWithQuality(image, 1f);
                if (encodedFrame != null) {
                    // 如果队列满了，移除最旧的帧
                    if (frameQueue.size() >= 1024) {
                        frameQueue.poll();
                    }
                    frameQueue.offer(encodedFrame);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //  将图像转换为JPEG并设置质量
    public static byte[] convertToJpegWithQuality(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (!writers.hasNext()) {
                throw new IllegalStateException("No JPEG Image Writer found");
            }

            ImageWriter writer = writers.next();
            ImageOutputStream output = ImageIO.createImageOutputStream(byteArrayOutputStream);
            writer.setOutput(output);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality); // 0.0f ~ 1.0f，越高质量越好

            writer.write(null, new IIOImage(image, null, null), param);

            output.close();
            writer.dispose();

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取下一个视频帧
     * @return 视频帧数据
     */
    public byte[] getNextFrame() {
        try {
            return frameQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
