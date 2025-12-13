package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.BusinessHoursResponse;
import com.trimminflow.demo.dto.SetBusinessHoursRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.BusinessHours;
import com.trimminflow.demo.entity.DayOfWeek;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.BusinessHoursRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BusinessHoursServiceTest {

    @Mock
    private BusinessHoursRepository businessHoursRepository;

    @Mock
    private BarbershopRepository barbershopRepository;

    @InjectMocks
    private BusinessHoursService businessHoursService;

    private Barbershop mockBarbershop;
    private final UUID BARBERSHOP_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockBarbershop = new Barbershop();
        mockBarbershop.setId(BARBERSHOP_ID);
    }

    @Test
    void setBusinessHours_ShouldSetOpenHours_WhenValid() {
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(LocalTime.of(17, 0));

        given(barbershopRepository.findById(BARBERSHOP_ID)).willReturn(Optional.of(mockBarbershop));
        given(businessHoursRepository.findByBarbershopIdAndDayOfWeek(BARBERSHOP_ID, DayOfWeek.MONDAY))
                .willReturn(Optional.empty()); // New entry

        given(businessHoursRepository.save(any(BusinessHours.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        BusinessHoursResponse response = businessHoursService.setBusinessHours(BARBERSHOP_ID, request);

        assertNotNull(response);
        assertEquals(DayOfWeek.MONDAY.name(), response.getDayOfWeek());
        assertTrue(response.getIsOpen());
        assertEquals(LocalTime.of(9, 0), response.getOpenTime());
        assertEquals(LocalTime.of(17, 0), response.getCloseTime());
    }

    @Test
    void setBusinessHours_ShouldSetClosed_WhenIsOpenFalse() {
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.SUNDAY);
        request.setIsOpen(false);

        given(barbershopRepository.findById(BARBERSHOP_ID)).willReturn(Optional.of(mockBarbershop));
        given(businessHoursRepository.findByBarbershopIdAndDayOfWeek(BARBERSHOP_ID, DayOfWeek.SUNDAY))
                .willReturn(Optional.empty());
        given(businessHoursRepository.save(any(BusinessHours.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        BusinessHoursResponse response = businessHoursService.setBusinessHours(BARBERSHOP_ID, request);

        assertFalse(response.getIsOpen());
        assertNull(response.getOpenTime());
        assertNull(response.getCloseTime());
    }

    @Test
    void setBusinessHours_ShouldThrowException_WhenOpenTimeAfterCloseTime() {
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(18, 0)); // 6 PM
        request.setCloseTime(LocalTime.of(9, 0)); // 9 AM

        given(barbershopRepository.findById(BARBERSHOP_ID)).willReturn(Optional.of(mockBarbershop));

        assertThrows(RuntimeException.class, () -> businessHoursService.setBusinessHours(BARBERSHOP_ID, request));
    }

    @Test
    void setBusinessHours_ShouldThrowException_WhenOpenLessThanOneHour() {
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(LocalTime.of(9, 30)); // Only 30 mins

        given(barbershopRepository.findById(BARBERSHOP_ID)).willReturn(Optional.of(mockBarbershop));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> businessHoursService.setBusinessHours(BARBERSHOP_ID, request));
        assertEquals("Business must be open for at least 1 hour", exception.getMessage());
    }
}
