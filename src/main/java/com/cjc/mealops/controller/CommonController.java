package com.cjc.mealops.controller;

import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.common.R;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/common")
public class CommonController {
    private final Path uploadRoot;

    public CommonController(@Value("${mealops.upload-dir:target/uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String filename = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(uploadRoot);
            file.transferTo(uploadRoot.resolve(filename));
            return R.success(Map.of("name", filename, "url", "/common/download?name=" + filename));
        } catch (IOException ex) {
            throw new BusinessException("文件上传失败");
        }
    }

    @GetMapping("/download")
    public void download(@RequestParam String name, HttpServletResponse response) {
        Path file = uploadRoot.resolve(name).normalize();
        if (!file.startsWith(uploadRoot) || !Files.isRegularFile(file)) {
            throw new BusinessException("文件不存在");
        }
        try {
            String contentType = Files.probeContentType(file);
            response.setContentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + file.getFileName() + "\"");
            try (OutputStream outputStream = response.getOutputStream()) {
                Files.copy(file, outputStream);
            }
        } catch (IOException ex) {
            throw new BusinessException("文件下载失败");
        }
    }
}
