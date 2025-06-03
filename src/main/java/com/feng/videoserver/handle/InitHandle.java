package com.feng.videoserver.handle;

import com.feng.videoserver.gboal.GlobalImageCache;
import com.feng.videoserver.service.impl.MJpegService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class InitHandle {

    @Autowired
    private MJpegService mJpegService;

    @Autowired
    private GlobalImageCache imageCacheService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {

        mJpegService.startEncodingThread();
        // 定期清理过期的图像（每分钟检查一次）
        scheduler.scheduleAtFixedRate(imageCacheService::cleanupExpiredImages, 1, 1, TimeUnit.MINUTES);

    }

    @PreDestroy
    public void destroy() {
        mJpegService.stopEncodingThread();
    }

}
