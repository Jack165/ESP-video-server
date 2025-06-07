package com.feng.videoserver.service.impl;
/*
import com.feng.videoserver.Socket.VideoClientHandler;
import com.feng.videoserver.gboal.GlobalImageCache;
import com.feng.videoserver.service.IVideoService;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

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

@Service
public class VideoStreamService implements IVideoService {

    @Autowired
    private GlobalImageCache imageCacheService;

    private final BlockingQueue<byte[]> frameQueue = new LinkedBlockingQueue<>(100);
    private volatile boolean running = false;
    private Thread encodingThread;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static final int STREAM_PORT = 8004;
    private volatile FFmpegFrameRecorder recorder;
    private ByteArrayOutputStream outputStream;
    private Java2DFrameConverter converter;

    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;

    @PreDestroy
    public void destroy() {
        stopEncodingThread();
    }

    /**
     * 添加图像到视频流
     * @param image 图像

    public void addImageToStream(BufferedImage image) {
        if (running && image != null) {
            try {
                // 将图像编码为H.265帧并添加到队列
                //byte[] encodedFrame = encodeImageToH265(image);
                byte[] encodedFrame=convertToJpegWithQuality(image, 0.75f);
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
      */

    /*
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

    public byte[] getNextFrame() {
        try {
            return frameQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
     */

    /**
     * 将图像编码为H.265帧
     * @param image 图像
     * @return 编码后的帧数据
     */
    /*
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
            // 增加关键帧间隔配置
            recorder.setVideoOption("x265-params", "keyint=60:min-keyint=30");
            
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
    */

    /*
    @Override
    public void start() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(STREAM_PORT)) {

                while (true) {
                    Socket client = server.accept();
                    threadPool.execute(new VideoClientHandler(client,imageCacheService));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

*/

    /**
     * 启动编码线程
     */
    /*
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

    */
    /**
     * 停止编码线程
     */
    /*
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
    */

    /*
    private byte[] encodeImageToFmp4(BufferedImage image) {
        try {
            // 清空旧数据
            outputStream.reset();

            // 编码帧
            recorder.record(converter.convert(image));

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    */

    /*
    private FFmpegFrameRecorder createFmp4Recorder(ByteArrayOutputStream outputStream, int width, int height) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, width, height);

        // 设置为 fMP4 模式（fragmented MP4）
        recorder.setFormat("mp4");
        recorder.setVideoCodecName("libx264"); // 或 libx265
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setOption("movflags", "+frag_keyframe+empty_moov"); // 关键配置

        // 编码参数
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("crf", "23");
        recorder.setVideoOption("x264-params", "keyint=60:min-keyint=30");

        try {
            recorder.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start FFmpegFrameRecorder", e);
        }

        return recorder;
    }

}

    */
