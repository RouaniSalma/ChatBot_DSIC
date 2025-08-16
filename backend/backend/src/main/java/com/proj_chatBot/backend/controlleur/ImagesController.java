package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/images")
public class ImagesController {

    @Autowired
    private FileStorageService storageService;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Resource file = storageService.load(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE,
                            Files.probeContentType(file.getFile().toPath()))
                    .header("Access-Control-Allow-Origin", "*") // Autorise CORS
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS)) // Cache
                    .body(file);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
