package com.medchart.ehr.service;

import com.medchart.ehr.legacy.InsuranceCache;
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
                .patientSsn("XXX-XX-0001")
                .build();
    }

    @Test
    void eligibilityCheckUsesCacheWhenAvailable() {
        InsuranceCache.CachedEligibility cached = new InsuranceCache.CachedEligibility();
        cached.eligible = true;
        cached.memberId = "INS999";
        cached.planName = "Cached Plan";
        cached.copay = "10.00";
        cached.deductible = "250.00";
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(cached);

        AppointmentService.EligibilityResult result =
                appointmentService.checkInsuranceEligibility("MRN001", "PAYER1");

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getMemberId()).isEqualTo("INS999");
        assertThat(result.getCopayRequired()).isEqualByComparingTo("10.00");
        assertThat(result.getDeductibleRemaining()).isEqualByComparingTo("250.00");
        verifyNoInteractions(insuranceGateway);
    }

    @Test
    void eligibilityCheckCallsGatewayAndCachesOnCacheMiss() {
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(null);
        when(insuranceGateway.verifyEligibility("MRN001", "PAYER1")).thenReturn(eligible());

        AppointmentService.EligibilityResult result =
                appointmentService.checkInsuranceEligibility("MRN001", "PAYER1");

        assertThat(result.getPlanName()).isEqualTo("Premium Health Plan");
        verify(insuranceCache).cacheEligibility(
                "MRN001", "XXX-XX-0001", "PAYER1", "INS123", true,
                "Premium Health Plan", "25.00", "500.00");
    }

    @Test
    void ineligibleGatewayResponseIsNotCached() {
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(null);
        when(insuranceGateway.verifyEligibility("MRN001", "PAYER1"))
                .thenReturn(AppointmentService.EligibilityResult.builder()
                        .eligible(false)
                        .reason("Coverage terminated")
                        .build());

        AppointmentService.EligibilityResult result =
                appointmentService.checkInsuranceEligibility("MRN001", "PAYER1");

        assertThat(result.isEligible()).isFalse();
        verify(insuranceCache, never()).cacheEligibility(
                anyString(), anyString(), anyString(), anyString(), anyBoolean(),
                anyString(), anyString(), anyString());
    }

    @Test
    void scheduleAppointmentReturnsVerifiedAppointment() {
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(null);
        when(insuranceGateway.verifyEligibility("MRN001", "PAYER1")).thenReturn(eligible());
        LocalDate date = LocalDate.of(2024, 6, 1);

        Map<String, Object> appointment = appointmentService.scheduleAppointment(
                1L, "MRN001", "PAYER1", date, "OFFICE_VISIT", 7L);

        assertThat(appointment)
                .containsEntry("patientId", 1L)
                .containsEntry("patientMrn", "MRN001")
                .containsEntry("appointmentDate", date)
                .containsEntry("appointmentType", "OFFICE_VISIT")
                .containsEntry("providerId", 7L)
                .containsEntry("eligibilityVerified", true)
                .containsEntry("status", "SCHEDULED");
        assertThat((BigDecimal) appointment.get("copayAmount")).isEqualByComparingTo("25.00");
    }

    @Test
    void scheduleAppointmentDoesNotLeakSsnIntoAppointmentRecord() {
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(null);
        when(insuranceGateway.verifyEligibility("MRN001", "PAYER1")).thenReturn(eligible());

        Map<String, Object> appointment = appointmentService.scheduleAppointment(
                1L, "MRN001", "PAYER1", LocalDate.of(2024, 6, 1), "OFFICE_VISIT", 7L);

        assertThat(appointment.values()).doesNotContain("XXX-XX-0001");
    }

    @Test
    void scheduleAppointmentRejectsIneligiblePatient() {
        when(insuranceCache.getEligibility("MRN001", "PAYER1")).thenReturn(null);
        when(insuranceGateway.verifyEligibility(any(), any()))
                .thenReturn(AppointmentService.EligibilityResult.builder()
                        .eligible(false)
                        .reason("Coverage terminated")
                        .build());

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(
                1L, "MRN001", "PAYER1", LocalDate.of(2024, 6, 1), "OFFICE_VISIT", 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Coverage terminated");
    }
}
