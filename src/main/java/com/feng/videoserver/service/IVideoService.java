package com.feng.videoserver.service;

import java.awt.image.BufferedImage;

public interface IVideoService {

    /**
     * 启动服务
     */
    public void start();

    /**
     *  启动编码线程
     */
    public void startEncodingThread();

    /**
     * 停止编码线程
     */
    public void stopEncodingThread();

    /**
     * 将图像添加到流中
     * @param image
     */
    public void addImageToStream(BufferedImage image);

    /**
     * 获取下一帧图像数据
     * @return
     */
    public byte[] getNextFrame();
}
