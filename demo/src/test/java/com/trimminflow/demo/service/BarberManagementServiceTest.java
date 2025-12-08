package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.BarberResponse;
import com.trimminflow.demo.dto.CreateBarberRequest;
import com.trimminflow.demo.dto.UpdateBarberRequest;
import com.trimminflow.demo.entity.Barber;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.repository.BarberRepository;
import com.trimminflow.demo.repository.BarbershopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BarberManagementServiceTest {

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private BarbershopRepository barbershopRepository;

    @InjectMocks
    private BarberManagementService barberManagementService;

    private Barbershop barbershop;
    private Barber barber;
    private UUID barbershopId;
    private UUID barberId;

    @BeforeEach
    void setUp() {
        barbershopId = UUID.randomUUID();
        barbershop = new Barbershop();
        barbershop.setId(barbershopId);
        barbershop.setName("Test Shop");

        barberId = UUID.randomUUID();
        barber = new Barber(barbershop, "John", "Doe", "john@example.com", "123456789", "Bio");
        barber.setId(barberId);
    }

    @Test
    void createBarber_Success() {
        CreateBarberRequest request = new CreateBarberRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");

        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(barbershop));
        when(barberRepository.existsByEmailAndBarbershopId(anyString(), eq(barbershopId))).thenReturn(false);
        when(barberRepository.save(any(Barber.class))).thenReturn(barber);

        BarberResponse response = barberManagementService.createBarber(barbershopId, request);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        verify(barberRepository).save(any(Barber.class));
    }

    @Test
    void createBarber_EmailExists_ThrowsException() {
        CreateBarberRequest request = new CreateBarberRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");

        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(barbershop));
        when(barberRepository.existsByEmailAndBarbershopId(anyString(), eq(barbershopId))).thenReturn(true);

        assertThrows(RuntimeException.class, () -> barberManagementService.createBarber(barbershopId, request));
    }

    @Test
    void updateBarber_RemoveProfileImage_Success() {
        UpdateBarberRequest request = new UpdateBarberRequest();
        request.setRemoveProfileImage(true);

        barber.setProfileImageUrl("http://example.com/image.jpg");
        when(barberRepository.findByIdAndBarbershopId(barberId, barbershopId)).thenReturn(Optional.of(barber));
        when(barberRepository.save(any(Barber.class))).thenReturn(barber);

        BarberResponse response = barberManagementService.updateBarber(barberId, barbershopId, request);

        assertNull(response.getProfileImageUrl());
    }

    @Test
    void deleteBarber_Success() {
        when(barberRepository.findByIdAndBarbershopId(barberId, barbershopId)).thenReturn(Optional.of(barber));
        when(barberRepository.save(any(Barber.class))).thenReturn(barber);

        barberManagementService.deleteBarber(barberId, barbershopId);

        assertFalse(barber.getIsActive());
        verify(barberRepository).save(barber);
    }
}
