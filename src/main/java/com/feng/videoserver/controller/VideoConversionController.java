package com.feng.videoserver.controller;

import com.feng.videoserver.model.VideoConversionRequest;
import com.feng.videoserver.model.VideoConversionResponse;
import com.feng.videoserver.service.ImageCacheService;
import com.feng.videoserver.service.VideoConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoConversionController {

    @Autowired
    private VideoConversionService videoConversionService;
    
    @Autowired
    private ImageCacheService imageCacheService;

    @PostMapping("/convert")
    public VideoConversionResponse convertJpegToH265Video(@RequestBody VideoConversionRequest request) {
        return videoConversionService.convertJpegToH265Video(request);
    }
    
    @PostMapping("/upload-convert")
    public VideoConversionResponse uploadAndConvert(
            @RequestParam("images") MultipartFile[] imageFiles,
            @RequestParam(value = "outputFileName", required = false) String outputFileName,
            @RequestParam(value = "frameRate", defaultValue = "30") int frameRate,
            @RequestParam(value = "width", defaultValue = "0") int width,
            @RequestParam(value = "height", defaultValue = "0") int height) throws IOException {
        
        List<byte[]> jpegImages = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            jpegImages.add(file.getBytes());
        }
        
        VideoConversionRequest request = new VideoConversionRequest();
        request.setJpegImages(jpegImages);
        request.setOutputFileName(outputFileName);
        request.setFrameRate(frameRate);
        request.setWidth(width);
        request.setHeight(height);
        
        return videoConversionService.convertJpegToH265Video(request);
    }
    
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            String imageId = imageCacheService.cacheImage(image);
            
            Map<String, String> response = new HashMap<>();
            response.put("imageId", imageId);
            response.put("message", "图像已成功缓存");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("error", "图像处理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadVideo(@PathVariable String fileName) {
        File file = new File("videos/" + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}