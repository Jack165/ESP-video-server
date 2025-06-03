package com.feng.videoserver.model;

public class VideoConversionResponse {
    private boolean success;
    private String message;
    private String videoPath;
    
    // 构造函数、getter和setter
    public VideoConversionResponse() {
    }
    
    public VideoConversionResponse(boolean success, String message, String videoPath) {
        this.success = success;
        this.message = message;
        this.videoPath = videoPath;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getVideoPath() {
        return videoPath;
    }
    
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
}