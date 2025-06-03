package com.feng.videoserver.Socket;

import com.feng.videoserver.gboal.GlobalImageCache;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private String deviceId;
    GlobalImageCache imageCacheService;
    public ClientHandler(Socket socket, GlobalImageCache imageCacheService) {
        this.socket = socket;
        this.imageCacheService=imageCacheService;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            // 读取设备MAC地址
            byte[] macBytes = new byte[12];
            in.readFully(macBytes);
            deviceId = new String(macBytes);
            System.out.println("ESP摄像头设备连接: " + deviceId);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int b;
            int matchIndex = 0;
            byte[] endMarker = {'j','p','e','g','\n'};

            while ((b = in.read()) != -1) {
                buffer.write(b);

                // 检测帧结束标记
                if (b == endMarker[matchIndex]) {
                    if (++matchIndex == endMarker.length) {
                        saveImage(buffer.toByteArray());
                        buffer.reset();
                        matchIndex = 0;
                    }
                } else {
                    matchIndex = 0;
                }
            }
        } catch (Exception e) {
            System.err.println("设备 断开: " + e.getMessage());
        }
    }

    private void saveImage(byte[] data) throws IOException {


        //String filename = STORAGE_PATH + deviceId + "_" + LocalDateTime.now().toString().replace(":", "-") + ".jpg";

        //try (FileOutputStream fos = new FileOutputStream(filename)) {
        //   fos.write(data, 0, data.length - 5); // 去除结束标记
        // }
        // System.out.println("保存 capture: " + filename);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        System.out.println("缓存图片! "+imageCacheService.getAllCachedImages().size());
        // 使用 ImageIO 读取图像
        BufferedImage bufferedImage = ImageIO.read(bis);
        String imageId = imageCacheService.cacheImage(bufferedImage);
    }
}
