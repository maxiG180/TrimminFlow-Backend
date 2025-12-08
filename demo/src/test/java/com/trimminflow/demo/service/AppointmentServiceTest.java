package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.AppointmentResponse;
import com.trimminflow.demo.dto.CreateAppointmentRequest;
import com.trimminflow.demo.entity.*;
import com.trimminflow.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private BarberRepository barberRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private BarbershopRepository barbershopRepository;
    @Mock
    private BusinessHoursRepository businessHoursRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AppointmentService appointmentService;

    private Barbershop barbershop;
    private Barber barber;
    private Service service;
    private UUID barbershopId;
    private UUID barberId;
    private UUID serviceId;

    @BeforeEach
    void setUp() {
        barbershopId = UUID.randomUUID();
        barbershop = new Barbershop();
        barbershop.setId(barbershopId);
        barbershop.setName("Test Shop");

        barberId = UUID.randomUUID();
        barber = new Barber(barbershop, "John", "Doe", "john@example.com", "123456789", "Bio");
        barber.setId(barberId);

        serviceId = UUID.randomUUID();
        service = new Service(barbershop, "Haircut", "Basic Haircut", BigDecimal.valueOf(25.0), 30);
        service.setId(serviceId);
    }

    @Test
    void createAppointment_Success() {
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setBarberId(barberId);
        request.setServiceId(serviceId);
        request.setAppointmentDateTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        request.setCustomerName("Jane Doe");
        request.setCustomerEmail("jane@example.com");
        request.setCustomerPhone("987654321");

        BusinessHours businessHours = new BusinessHours();
        businessHours.setIsOpen(true);
        businessHours.setOpenTime(LocalTime.of(9, 0));
        businessHours.setCloseTime(LocalTime.of(18, 0));

        when(barberRepository.findById(barberId)).thenReturn(Optional.of(barber));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(barbershop));
        when(businessHoursRepository.findByBarbershopIdAndDayOfWeek(any(), any()))
                .thenReturn(Optional.of(businessHours));
        when(appointmentRepository.findConflictingAppointments(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = appointmentService.createAppointment(barbershopId, request);

        assertNotNull(response);
        assertEquals("Jane Doe", response.getCustomerName());
        verify(messagingTemplate).convertAndSend(eq("/topic/appointments"), any(AppointmentResponse.class));
    }

    @Test
    void cancelAppointment_Success() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = new Appointment(barbershop, barber, service, LocalDateTime.now().plusDays(1), "Jane",
                "jane@example.com", "123");
        appointment.setId(appointmentId);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        appointmentService.cancelAppointment(barbershopId, appointmentId);

        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        verify(messagingTemplate).convertAndSend(eq("/topic/appointments"), any(AppointmentResponse.class));
    }
}
