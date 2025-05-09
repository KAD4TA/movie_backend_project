package com.film_backend.film.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUtil {

    
    private final long MAX_FILE_SIZE;
    private final String uploadDir;

    public ImageUtil(@Value("${file.max-size:10485760}") long maxFileSize, 
            @Value("${file.upload-dir:uploads/}") String uploadDir) {
    	this.MAX_FILE_SIZE = maxFileSize;
            this.uploadDir = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
}

    /**
     * Saves an image from a URL or local file path and returns the saved file path.
     * @param imageSource The URL or local file path of the image.
     * @param username The username for naming the file.
     * @return The path where the image is saved.
     * @throws IOException If there is an error reading or saving the file.
     */
    public String saveImage(String imageSource, String username) throws IOException {
        if (imageSource == null || imageSource.trim().isEmpty()) {
           
            throw new IllegalArgumentException("Image source cannot be null or empty");
        }

        String fileExtension;
        Path targetPath;

        // URL format check
        if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
            try {
                // Use URI.toURL() instead of deprecated URL constructor
                URL url = URI.create(imageSource).toURL();
                String path = url.getPath();
                fileExtension = path.substring(path.lastIndexOf(".")).toLowerCase();
                if (!fileExtension.equals(".jpg") && !fileExtension.equals(".jpeg") && !fileExtension.equals(".png")) {
                    
                    throw new IllegalArgumentException("Unsupported file type: Only JPG and PNG are supported.");
                }

                String newFileName = username + "_" + UUID.randomUUID() + fileExtension;
                targetPath = Paths.get(uploadDir, newFileName);
                Files.createDirectories(targetPath.getParent());

                // Download and save the image
                try (var inputStream = url.openStream()) {
                    long size = Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    if (size > MAX_FILE_SIZE) {
                        Files.deleteIfExists(targetPath);
                        
                        throw new IllegalArgumentException("Image file is too large: Maximum size is 10 MB.");
                    }
                }
            } catch (MalformedURLException e) {
                
                throw new IOException("Invalid URL format: " + e.getMessage());
            } catch (IOException e) {
                
                throw new IOException("Failed to download image from URL: " + e.getMessage());
            }
        }
        // Local file path control
        else {
            Path sourcePath;
            try {
                sourcePath = Paths.get(imageSource);
            } catch (Exception e) {
                
                throw new IllegalArgumentException("Invalid file path: " + imageSource);
            }

            // Security check Block access to system directories
            if (sourcePath.toAbsolutePath().normalize().startsWith(Paths.get("C:\\Windows").normalize())) {
               
                throw new IllegalArgumentException("Access to system directories is not allowed.");
            }

            if (!Files.exists(sourcePath)) {
                
                throw new IOException("Source file not found: " + imageSource);
            }
            if (Files.size(sourcePath) > MAX_FILE_SIZE) {
                
                throw new IllegalArgumentException("Image file is too large: Maximum size is 10 MB.");
            }

            fileExtension = imageSource.substring(imageSource.lastIndexOf(".")).toLowerCase();
            if (!fileExtension.equals(".jpg") && !fileExtension.equals(".jpeg") && !fileExtension.equals(".png")) {
               
                throw new IllegalArgumentException("Unsupported file type: Only JPG and PNG are supported.");
            }

            String newFileName = username + "_" + UUID.randomUUID() + fileExtension;
            targetPath = Paths.get(uploadDir, newFileName);
            Files.createDirectories(targetPath.getParent());
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        String savedPath = targetPath.toString();
       
        return savedPath;
    }
}