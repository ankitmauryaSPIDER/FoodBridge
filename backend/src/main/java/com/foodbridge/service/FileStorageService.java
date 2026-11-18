package com.foodbridge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp");
    private final Path uploadPath;
    public FileStorageService(@Value("${app.upload-dir}") String directory) {
        this.uploadPath = Paths.get(directory).toAbsolutePath().normalize();
        try { Files.createDirectories(uploadPath); } catch (IOException e) { throw new IllegalStateException("Cannot create upload directory", e); }
    }
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an image file");
        if (file.getSize() > 5L * 1024 * 1024) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be 5 MB or smaller");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG and WebP images are allowed");
        String extension = switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
        String filename = UUID.randomUUID() + "." + extension;
        try { Files.copy(file.getInputStream(), uploadPath.resolve(filename)); }
        catch (IOException e) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image could not be saved"); }
        return "/uploads/" + filename;
    }
    public Path path() { return uploadPath; }
}
