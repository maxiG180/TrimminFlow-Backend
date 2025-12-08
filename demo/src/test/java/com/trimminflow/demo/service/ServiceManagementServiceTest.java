package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.CreateServiceRequest;
import com.trimminflow.demo.dto.ServiceResponse;
import com.trimminflow.demo.dto.UpdateServiceRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.Service;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceManagementServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private BarbershopRepository barbershopRepository;

    @InjectMocks
    private ServiceManagementService serviceManagementService;

    private Barbershop barbershop;
    private Service service;
    private UUID barbershopId;
    private UUID serviceId;

    @BeforeEach
    void setUp() {
        barbershopId = UUID.randomUUID();
        barbershop = new Barbershop();
        barbershop.setId(barbershopId);
        barbershop.setName("Test Shop");

        serviceId = UUID.randomUUID();
        service = new Service(barbershop, "Haircut", "Basic Haircut", BigDecimal.valueOf(25.0), 30);
        service.setId(serviceId);
    }

    @Test
    void createService_Success() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Haircut");
        request.setPrice(BigDecimal.valueOf(25.0));
        request.setDurationMinutes(30);

        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(barbershop));
        when(serviceRepository.existsByNameAndBarbershopId("Haircut", barbershopId)).thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        ServiceResponse response = serviceManagementService.createService(barbershopId, request);

        assertNotNull(response);
        assertEquals("Haircut", response.getName());
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void createService_DuplicateName_ThrowsException() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Haircut");
        request.setPrice(BigDecimal.valueOf(25.0));
        request.setDurationMinutes(30);

        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(barbershop));
        when(serviceRepository.existsByNameAndBarbershopId("Haircut", barbershopId)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> serviceManagementService.createService(barbershopId, request));
    }

    @Test
    void updateService_Success() {
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setName("Updated Haircut");
        request.setPrice(BigDecimal.valueOf(30.0));

        when(serviceRepository.findByIdAndBarbershopId(serviceId, barbershopId)).thenReturn(Optional.of(service));
        when(serviceRepository.existsByNameAndBarbershopIdExcludingId("Updated Haircut", barbershopId, serviceId))
                .thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        ServiceResponse response = serviceManagementService.updateService(serviceId, barbershopId, request);

        assertNotNull(response);
        verify(serviceRepository).save(service);
    }

    @Test
    void deleteService_Success() {
        when(serviceRepository.findByIdAndBarbershopId(serviceId, barbershopId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        serviceManagementService.deleteService(serviceId, barbershopId);

        assertFalse(service.getIsActive());
        verify(serviceRepository).save(service);
    }
}
