package com.proj_chatBot.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;


import org.springframework.util.FileSystemUtils;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${app.upload.dir}") String uploadPath) {
        this.rootLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Impossible de créer le dossier de stockage: " + rootLocation, e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Fichier vide");
            }

            // Sécurité: nettoyer le nom du fichier
            String originalName = StringUtils.cleanPath(
                    Objects.requireNonNull(file.getOriginalFilename()));

            // Générer un nom unique
            String newName = UUID.randomUUID() + "_" + originalName;
            Path destination = this.rootLocation.resolve(newName);

            // Vérifier qu'on ne sort pas du dossier autorisé
            if (!destination.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Tentative de stockage en dehors du dossier autorisé");
            }

            // Copier le fichier
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination,
                        StandardCopyOption.REPLACE_EXISTING);
            }

            return newName;
        } catch (IOException e) {
            throw new RuntimeException("Échec du stockage: " + e.getMessage(), e);
        }
    }

    public Resource load(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier non lisible: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur de lecture: " + filename, e);
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
}
