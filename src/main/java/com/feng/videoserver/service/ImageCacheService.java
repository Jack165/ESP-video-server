package com.feng.videoserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ImageCacheService {

    @Value("${image.cache.retention-minutes:10}")
    private int retentionMinutes;

    private final Map<String, CachedImage> imageCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        // 定期清理过期的图像（每分钟检查一次）
        scheduler.scheduleAtFixedRate(this::cleanupExpiredImages, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 添加图像到缓存
     * @param image 图像
     * @return 图像ID
     */
    public String cacheImage(BufferedImage image) {
        String imageId = UUID.randomUUID().toString();
        imageCache.put(imageId, new CachedImage(image, Instant.now()));
        return imageId;
    }

    /**
     * 获取缓存中的所有图像
     * @return 图像缓存
     */
    public Map<String, CachedImage> getAllCachedImages() {
        return imageCache;
    }

    /**
     * 清理过期的图像
     */
    private void cleanupExpiredImages() {
        Instant expirationTime = Instant.now().minus(Duration.ofMinutes(retentionMinutes));
        imageCache.entrySet().removeIf(entry -> entry.getValue().getTimestamp().isBefore(expirationTime));
    }

    /**
     * 缓存图像类
     */
    public static class CachedImage {
        private final BufferedImage image;
        private final Instant timestamp;

        public CachedImage(BufferedImage image, Instant timestamp) {
            this.image = image;
            this.timestamp = timestamp;
        }

        public BufferedImage getImage() {
            return image;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }
}