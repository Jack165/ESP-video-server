package com.feng.videoserver.controller;

import com.feng.videoserver.model.Version;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/ota")
public class OTAController {
    @PostMapping("/version")
    public Version getVersion() {
        Version version = new Version();
        version.setVersion("1.0.0");
        version.setUrl("http://8.133.245.190/ota");
        log.info("查询版本...");
        return version;
    }
    @PostMapping("/update")
    public  ResponseEntity<Resource> getUpdate() {
        Resource resource =  new ClassPathResource("/static/firmware.bin");

        log.info("开始更新固件");


        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"firmware.bin\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
