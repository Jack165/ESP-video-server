package com.feng.videoserver.gboal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 全局图片缓存器
 */
@Service
public class GlobalImageCache {

    @Value("${image.cache.retention-minutes:10}")
    private int retentionMinutes;

    @Value("${image.cache.max-size:10000}")
    private int maxCacheSize;

    private final Map<String, CachedImage> imageCache = new ConcurrentHashMap<>();


    /**
     * 添加图像到缓存
     * @param image 图像
     * @return 图像ID
     */
    public String cacheImage(BufferedImage image) {
        // 新增缓存大小检查
        if (imageCache.size() >= maxCacheSize) {
            cleanupExpiredImages();
        }
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
    public void cleanupExpiredImages() {
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