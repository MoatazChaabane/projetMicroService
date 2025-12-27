package tn.pi.back.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    
    private final Path fileStorageLocation;
    
    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage", ex);
        }
    }
    
    public String storeFile(MultipartFile file, String userId) {
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }
        
        // Vérifier le type de fichier
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Le fichier doit être une image");
        }
        
        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = userId + "_" + UUID.randomUUID() + extension;
        
        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Retourner l'URL relative
            return "/uploads/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Impossible de stocker le fichier", ex);
        }
    }
    
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.warn("Impossible de supprimer le fichier: {}", fileUrl, ex);
        }
    }
}

