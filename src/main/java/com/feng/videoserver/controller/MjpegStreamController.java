package com.feng.videoserver.controller;

import com.feng.videoserver.gboal.GlobalImageCache;
import com.feng.videoserver.service.impl.MJpegService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@RequestMapping("/api/JPEG")
public class MjpegStreamController {

    @Autowired
    private MJpegService mJpegService;
    @Autowired
    private GlobalImageCache imageCacheService;

    @GetMapping(value = "/stream", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void streamMjpeg(HttpServletResponse response) throws IOException {
        response.setContentType("multipart/x-mixed-replace;boundary=--myboundary");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");

        ServletOutputStream outputStream = response.getOutputStream();

        while (true) {
            byte[] jpegFrame = mJpegService.getNextFrame(); // 确保获取的是 JPEG 图像
            if (jpegFrame != null) {
                writeBoundary(outputStream);
                writeJpegFrame(outputStream, jpegFrame);
            }

            try {
               // Thread.sleep(33); // ~30fps
                Thread.sleep(5); // ~10fps
            } catch (InterruptedException e) {
                break;
            }
        }

        outputStream.close();
    }

    private void writeBoundary(ServletOutputStream out) throws IOException {
        out.write("\r\n--myboundary\r\n".getBytes());
        out.write("Content-Type: image/jpeg\r\n\r\n".getBytes());
    }

    private void writeJpegFrame(ServletOutputStream out, byte[] jpegData) throws IOException {
        out.write(jpegData);
    }
    @GetMapping("/start")
    public String start(){
        mJpegService.start();
        return "success";
    }
}
