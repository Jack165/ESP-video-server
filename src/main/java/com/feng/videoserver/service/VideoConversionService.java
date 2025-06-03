package com.feng.videoserver.service;

import com.feng.videoserver.model.VideoConversionRequest;
import com.feng.videoserver.model.VideoConversionResponse;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.bytedeco.opencv.global.opencv_imgproc.resize;

@Service
public class VideoConversionService {

    @Value("${video.output.directory:videos}")
    private String outputDirectory;

    public VideoConversionResponse convertJpegToH265Video(VideoConversionRequest request) {
        try {
            // 确保输出目录存在
            Path outputPath = Paths.get(outputDirectory);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            // 生成唯一的输出文件名
            String outputFileName = request.getOutputFileName();
            if (outputFileName == null || outputFileName.isEmpty()) {
                outputFileName = UUID.randomUUID().toString() + ".mp4";
            } else if (!outputFileName.endsWith(".mp4")) {
                outputFileName += ".mp4";
            }

            File outputFile = new File(outputPath.toFile(), outputFileName);
            String outputFilePath = outputFile.getAbsolutePath();

            // 设置帧率
            int frameRate = request.getFrameRate() > 0 ? request.getFrameRate() : 30;
            
            // 获取图像尺寸
            int width = request.getWidth();
            int height = request.getHeight();
            
            // 如果没有指定尺寸，从第一张图片获取
            if (width <= 0 || height <= 0) {
                BufferedImage firstImage = ImageIO.read(new ByteArrayInputStream(request.getJpegImages().get(0)));
                width = firstImage.getWidth();
                height = firstImage.getHeight();
            }

            // 创建转换器
            Java2DFrameConverter java2DConverter = new Java2DFrameConverter();
            OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();

            // 创建视频录制器
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFilePath, width, height);
            recorder.setVideoCodec(8); // HEVC (H.265)
            recorder.setFormat("mp4");
            recorder.setFrameRate(frameRate);
            recorder.setPixelFormat(0); // YUV420P
            recorder.setVideoQuality(0); // 最佳质量
            recorder.start();

            // 处理每一帧
            List<byte[]> jpegImages = request.getJpegImages();
            for (byte[] jpegData : jpegImages) {
                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(jpegData));
                
                // 如果图像尺寸与设定不符，进行调整
                if (bufferedImage.getWidth() != width || bufferedImage.getHeight() != height) {
                    Mat mat = matConverter.convert(java2DConverter.convert(bufferedImage));
                    Mat resizedMat = new Mat();
                    resize(mat, resizedMat, new Size(width, height));
                    recorder.record(matConverter.convert(resizedMat));
                } else {
                    recorder.record(java2DConverter.convert(bufferedImage));
                }
            }

            // 关闭录制器
            recorder.stop();
            recorder.release();

            return new VideoConversionResponse(true, "视频转换成功", outputFilePath);

        } catch (Exception e) {
            e.printStackTrace();
            return new VideoConversionResponse(false, "视频转换失败: " + e.getMessage(), null);
        }
    }
}