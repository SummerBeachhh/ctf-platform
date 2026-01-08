package com.ctf.platform.controller;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/api/thumbnail")
public class ThumbnailController {

    @GetMapping("/{filename:.+}")
    @ResponseBody
    public ResponseEntity<byte[]> getThumbnail(@PathVariable String filename) {
        try {
            String projectRoot = System.getProperty("user.dir");
            // Basic path traversal protection
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                return ResponseEntity.badRequest().build();
            }
            
            Path filePath = Paths.get(projectRoot, "uploads", filename);
            File file = filePath.toFile();

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Cache directory
            File cacheDir = new File(projectRoot, "cache/thumbnails");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File cacheFile = new File(cacheDir, filename);
            
            // If cache exists, serve it
            if (cacheFile.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .header("Cache-Control", "max-age=86400")
                        .body(java.nio.file.Files.readAllBytes(cacheFile.toPath()));
            }

            // Generate cache
            Thumbnails.of(file)
                    .size(200, 200)
                    .outputQuality(0.3)
                    .outputFormat("jpg") // Force JPEG for consistency and size
                    .toFile(cacheFile);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header("Cache-Control", "max-age=86400")
                    .body(java.nio.file.Files.readAllBytes(cacheFile.toPath()));

        } catch (Exception e) {
            // e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
