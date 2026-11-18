package com.foodbridge.controller;

import com.foodbridge.service.FileStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final FileStorageService storage;
    public UploadController(FileStorageService storage) { this.storage = storage; }
    @PostMapping("/image") public Map<String, String> upload(@RequestParam("file") MultipartFile file, Authentication auth) {
        return Map.of("url", storage.store(file));
    }
}
