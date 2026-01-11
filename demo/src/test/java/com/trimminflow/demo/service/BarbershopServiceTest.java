package com.trimminflow.demo.service;

import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.repository.BarbershopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarbershopServiceTest {

    @Mock
    private BarbershopRepository barbershopRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private BarbershopService barbershopService;

    private UUID barbershopId;
    private Barbershop barbershop;

    @BeforeEach
    void setUp() {
        barbershopId = UUID.randomUUID();
        barbershop = new Barbershop();
        barbershop.setId(barbershopId);
        barbershop.setName("Test Barbershop");
        barbershop.setAddress("123 Test St");
    }

    @Test
    void getBarbershopById_ReturnsSuccessfully() {
        // Given
        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(barbershop));

        // When
        Optional<Barbershop> result = barbershopService.getBarbershopById(barbershopId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(barbershopId, result.get().getId());
    }

    @Test
    void uploadLogo_Success() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.png",
                "image/png",
                "test image".getBytes());
        String logoUrl = "https://cloudinary.com/logo.png";

        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(barbershop));
        when(cloudinaryService.uploadImage(any())).thenReturn(logoUrl);
        when(barbershopRepository.save(any(Barbershop.class))).thenReturn(barbershop);

        // When
        String result = barbershopService.uploadLogo(barbershopId, file);

        // Then
        assertEquals(logoUrl, result);
        verify(cloudinaryService).uploadImage(file);
        verify(barbershopRepository).save(barbershop);
    }

    @Test
    void updateReminderSettings_Success() {
        // Given
        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(barbershop));
        when(barbershopRepository.save(any(Barbershop.class))).thenReturn(barbershop);

        // When
        Barbershop result = barbershopService.updateReminderSettings(barbershopId, true);

        // Then
        assertNotNull(result);
        verify(barbershopRepository).save(barbershop);
    }

    @Test
    void createBarbershop_Success() {
        // Given
        when(barbershopRepository.save(any(Barbershop.class))).thenReturn(barbershop);

        // When
        Barbershop result = barbershopService.createBarbershop(barbershop);

        // Then
        assertNotNull(result);
        assertEquals("Test Barbershop", result.getName());
    }
}
