package com.medchart.ehr.service;

import com.medchart.ehr.legacy.InsuranceCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private InsuranceCache insuranceCache;

    @Mock
    private InsuranceGateway insuranceGateway;

    @InjectMocks
    private AppointmentService appointmentService;

    private static AppointmentService.EligibilityResult eligible() {
        return AppointmentService.EligibilityResult.builder()
                .eligible(true)
                .memberId("INS123")
                .planName("Premium Health Plan")
                .copayRequired(new BigDecimal("25.00"))
                .deductibleRemaining(new BigDecimal("500.00"))
                .build();
    }

    @Test
    @DisplayName("checkInsuranceEligibility serves a cached entry without calling the gateway")
    void eligibilityIsServedFromCache() {
        InsuranceCache.CachedEligibility cached = new InsuranceCache.CachedEligibility();
        cached.eligible = true;
        cached.memberId = "INS123";
        cached.planName = "Premium Health Plan";
        cached.copay = "25.00";
        cached.deductible = "500.00";
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(cached);

        AppointmentService.EligibilityResult result =
                appointmentService.checkInsuranceEligibility("MRN001", "PAYER1");

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getMemberId()).isEqualTo("INS123");
        assertThat(result.getCopayRequired()).isEqualByComparingTo("25.00");
        verifyNoInteractions(insuranceGateway);
    }

    @Test
    @DisplayName("checkInsuranceEligibility falls back to the gateway and caches an eligible response")
    void eligibilityFallsBackToGatewayAndCaches() {
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(null);
        when(insuranceGateway.verifyEligibility("MRN001", "PAYER1")).thenReturn(eligible());

        AppointmentService.EligibilityResult result =
                appointmentService.checkInsuranceEligibility("MRN001", "PAYER1");

        assertThat(result.isEligible()).isTrue();
        verify(insuranceCache).cacheEligibility(
                "MRN001", null, "PAYER1", "INS123", true, "Premium Health Plan", "25.00", "500.00");
    }

    @Test
    @DisplayName("an ineligible gateway response is not cached")
    void ineligibleResponseIsNotCached() {
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(null);
        when(insuranceGateway.verifyEligibility("MRN001", "PAYER1")).thenReturn(
                AppointmentService.EligibilityResult.builder()
                        .eligible(false)
                        .reason("Coverage terminated")
                        .build());

        assertThat(appointmentService.checkInsuranceEligibility("MRN001", "PAYER1").isEligible()).isFalse();

        verify(insuranceCache, never()).cacheEligibility(
                anyString(), any(), anyString(), any(), anyBoolean(), any(), any(), any());
    }

    @Test
    @DisplayName("scheduleAppointment records the verified eligibility and copay")
    void scheduleAppointmentBuildsRecord() {
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(null);
        when(insuranceGateway.verifyEligibility("MRN001", "PAYER1")).thenReturn(eligible());
        LocalDate date = LocalDate.of(2024, 6, 1);

        Map<String, Object> appointment = appointmentService.scheduleAppointment(
                1L, "MRN001", "PAYER1", date, "OFFICE_VISIT", 3L);

        assertThat(appointment)
                .containsEntry("patientId", 1L)
                .containsEntry("patientMrn", "MRN001")
                .containsEntry("appointmentDate", date)
                .containsEntry("appointmentType", "OFFICE_VISIT")
                .containsEntry("providerId", 3L)
                .containsEntry("eligibilityVerified", true)
                .containsEntry("status", "SCHEDULED");
        assertThat((BigDecimal) appointment.get("copayAmount")).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("scheduleAppointment refuses to book when the patient is not eligible")
    void scheduleAppointmentRejectsIneligiblePatient() {
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(null);
        when(insuranceGateway.verifyEligibility("MRN001", "PAYER1")).thenReturn(
                AppointmentService.EligibilityResult.builder()
                        .eligible(false)
                        .reason("Coverage terminated")
                        .build());

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(
                1L, "MRN001", "PAYER1", LocalDate.of(2024, 6, 1), "OFFICE_VISIT", 3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Coverage terminated");
    }
}
