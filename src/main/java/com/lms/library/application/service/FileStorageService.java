package com.lms.library.application.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final String uploadDir;

    public FileStorageService() {
        // Fallback or mapped docker volume
        String mappedDir = "/app/uploads/covers";
        if (!new File("/app").exists()) {
            mappedDir = "uploads/covers"; 
        }
        this.uploadDir = mappedDir;
        
        // Ensure directory exists
        File dir = new File(this.uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) log.info("Created upload directory: {}", this.uploadDir);
        }
    }

    public String storeCoverImage(MultipartFile file) {
        try {
            validateImage(file);
            
            String extension = getExtensionByStringHandling(file.getOriginalFilename());
            if (extension.isEmpty()) extension = "jpg"; // fallback
            
            String fileName = UUID.randomUUID().toString() + "." + extension;
            Path destinationUrl = Paths.get(this.uploadDir).resolve(fileName);

            // Ensure directory exists again at runtime to be safe
            File dir = new File(this.uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // Resize and save using Thumbnailator
            Thumbnails.of(file.getInputStream())
                    .size(800, 1200) // Max dimensions, maintains aspect ratio
                    .outputQuality(0.85) // Compression
                    .toFile(destinationUrl.toFile());

            log.info("File successfully uploaded and resized to {}", destinationUrl);
            return "/uploads/covers/" + fileName;

        } catch (Exception e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Could not store image file", e);
        }
    }

    public String storeCoverImageFromUrl(String imageUrl) {
        try {
            URL url = URI.create(imageUrl).toURL();
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            String contentType = connection.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("The URL must point to an image");
            }
            if (connection.getContentLength() > 2 * 1024 * 1024) {
                 throw new IllegalArgumentException("Remote image file too large. Max 2MB allowed.");
            }

            try (InputStream in = connection.getInputStream()) {
                String extension = "jpg"; // default
                if (contentType.equals("image/png")) extension = "png";
                else if (contentType.equals("image/webp")) extension = "webp";
                
                String fileName = UUID.randomUUID().toString() + "." + extension;
                Path destinationUrl = Paths.get(this.uploadDir).resolve(fileName);
                
                Thumbnails.of(in)
                        .size(800, 1200)
                        .outputQuality(0.85)
                        .toFile(destinationUrl.toFile());
                        
                log.info("Remote file successfully downloaded and resized to {}", destinationUrl);
                return "/uploads/covers/" + fileName;
            }
        } catch (IllegalArgumentException e) {
             throw e;
        } catch (Exception e) {
            log.error("Failed to download image from URL", e);
            throw new RuntimeException("Could not download remote image file", e);
        }
    }

    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/covers/")) return;
        
        try {
            String fileName = fileUrl.substring("/uploads/covers/".length());
            Path filePath = Paths.get(this.uploadDir).resolve(fileName).normalize();
            
            // Delete actual file
            Files.deleteIfExists(filePath);
            log.info("Deleted old cover file: {}", filePath);
        } catch (Exception e) {
            log.warn("Failed to delete file: {}", fileUrl, e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }
        
        if (file.getSize() > 2L * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 2MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Only JPEG, PNG and WEBP file types are allowed");
        }
        
        // Deep validate checking real mime magic bits / stream validation by ImageIO later
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("The provided file is not a valid image");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid image content", e);
        }
    }
    
    private String getExtensionByStringHandling(String filename) {
        if (filename == null) return "";
        int lastPos = filename.lastIndexOf(".");
        if (lastPos < 0) return "";
        return filename.substring(lastPos + 1).toLowerCase();
    }
}
