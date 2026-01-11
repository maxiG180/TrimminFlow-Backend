package com.trimminflow.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void uploadImage_Success_ReturnsUrl() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://cloudinary.com/image.jpg");

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadImage(file);

        // Then
        assertNotNull(result);
        assertEquals("https://cloudinary.com/image.jpg", result);
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadImage_WithException_ThrowsRuntimeException() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new RuntimeException("Upload failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> cloudinaryService.uploadImage(file));
    }

    @Test
    void uploadImage_WithPngFile_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test image content".getBytes());

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://cloudinary.com/image.png");

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        // When
        String result = cloudinaryService.uploadImage(file);

        // Then
        assertEquals("https://cloudinary.com/image.png", result);
    }
}
