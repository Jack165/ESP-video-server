package com.feng.videoserver.service;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class VideoStreamService {

    @Autowired
    private ImageCacheService imageCacheService;

    private final BlockingQueue<byte[]> frameQueue = new LinkedBlockingQueue<>(100);
    private volatile boolean running = false;
    private Thread encodingThread;

    @PostConstruct
    public void init() {
        // 初始化FFmpeg日志
        FFmpegLogCallback.set();
        // 设置FFmpeg日志级别
        avutil.av_log_set_level(avutil.AV_LOG_INFO);
        
        startEncodingThread();
    }

    @PreDestroy
    public void destroy() {
        stopEncodingThread();
    }

    /**
     * 添加图像到视频流
     * @param image 图像
     */
    public void addImageToStream(BufferedImage image) {
        if (running && image != null) {
            try {
                // 将图像编码为H.265帧并添加到队列
                byte[] encodedFrame = encodeImageToH265(image);
                if (encodedFrame != null) {
                    // 如果队列满了，移除最旧的帧
                    if (frameQueue.size() >= 100) {
                        frameQueue.poll();
                    }
                    frameQueue.offer(encodedFrame);
                }
            } catch (Exception e) {
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

    /**
     * 将图像编码为H.265帧
     * @param image 图像
     * @return 编码后的帧数据
     */
    private byte[] encodeImageToH265(BufferedImage image) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, image.getWidth(), image.getHeight());
            
            // 使用编码器名称而不是编码器ID
            recorder.setVideoCodecName("libx265");
            // 替代方案：使用正确的常量指定编码器ID
            // recorder.setVideoCodec(avcodec.AV_CODEC_ID_HEVC);
            
            recorder.setFormat("matroska"); // 使用MKV容器格式，更适合流式传输
            recorder.setFrameRate(30);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            
            // 设置H.265编码参数
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setVideoOption("x265-params", "crf=23");
            
            recorder.start();
            recorder.record(converter.convert(image));
            recorder.stop();
            recorder.release();
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 启动编码线程
     */
    private void startEncodingThread() {
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
    private void stopEncodingThread() {
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
}