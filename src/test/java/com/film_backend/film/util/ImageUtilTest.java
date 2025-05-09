package com.film_backend.film.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImageUtilTest {

    @TempDir
    Path tempDir;

    private ImageUtil imageUtil;
    private String username;

    @BeforeEach
    void setUp() {
        
        imageUtil = new ImageUtil(10 * 1024 * 1024, tempDir.toString()); 
        username = "testuser";
    }

    @Test
    void saveImage_withValidJpgFile_shouldSaveSuccessfully() throws IOException {
        Path testImage = tempDir.resolve("test.jpg");
        Files.write(testImage, new byte[1024]);

        String savedPath = imageUtil.saveImage(testImage.toString(), username);

        assertThat(savedPath).contains(username).endsWith(".jpg");
        assertThat(Files.exists(Path.of(savedPath))).isTrue();
    }

    @Test
    void saveImage_withUnsupportedFileType_shouldThrowException() throws IOException {
        Path testImage = tempDir.resolve("test.gif");
        Files.write(testImage, new byte[1024]);

        assertThatThrownBy(() -> imageUtil.saveImage(testImage.toString(), username))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    void saveImage_withTooLargeFile_shouldThrowException() throws IOException {
        Path testImage = tempDir.resolve("large.jpg");
        Files.write(testImage, new byte[11 * 1024 * 1024]);

        assertThatThrownBy(() -> imageUtil.saveImage(testImage.toString(), username))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image file is too large");
    }

    @Test
    void saveImage_withNonExistentFile_shouldThrowException() {
        Path nonexistent = tempDir.resolve("nonexistent.jpg");

        assertThatThrownBy(() -> imageUtil.saveImage(nonexistent.toString(), username))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Source file not found");
    }

    @Test
    void saveImage_withNullSource_shouldThrowException() {
        assertThatThrownBy(() -> imageUtil.saveImage(null, username))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image source cannot be null");
    }

    @Test
    void saveImage_withPathToWindowsDirectory_shouldThrowException() {
        String windowsPath = "C:\\Windows\\System32\\image.jpg";

        assertThatThrownBy(() -> imageUtil.saveImage(windowsPath, username))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Access to system directories is not allowed");
    }
}