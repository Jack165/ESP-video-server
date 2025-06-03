package com.feng.videoserver.model;

import java.util.List;

public class VideoConversionRequest {
    private List<byte[]> jpegImages;
    private String outputFileName;
    private int frameRate;
    private int width;
    private int height;
    
    // 构造函数、getter和setter
    public VideoConversionRequest() {
    }
    
    public List<byte[]> getJpegImages() {
        return jpegImages;
    }
    
    public void setJpegImages(List<byte[]> jpegImages) {
        this.jpegImages = jpegImages;
    }
    
    public String getOutputFileName() {
        return outputFileName;
    }
    
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
    
    public int getFrameRate() {
        return frameRate;
    }
    
    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
}