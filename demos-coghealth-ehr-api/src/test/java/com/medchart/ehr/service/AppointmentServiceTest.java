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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final String MRN = "MRN-2019-00001";
    private static final String PAYER_ID = "AETNA";

    @Mock
    private InsuranceCache insuranceCache;

    @Mock
    private InsuranceGateway insuranceGateway;

    @InjectMocks
    private AppointmentService appointmentService;

    private AppointmentService.EligibilityResult eligible() {
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
    void checkInsuranceEligibility_usesCachedResultWithoutCallingGateway() {
        InsuranceCache.CachedEligibility cached = new InsuranceCache.CachedEligibility();
        cached.eligible = true;
        cached.memberId = "INS123";
        cached.planName = "Premium Health Plan";
        cached.copay = "25.00";
        cached.deductible = "500.00";
        when(insuranceCache.getEligibility(MRN, PAYER_ID)).thenReturn(cached);

        AppointmentService.EligibilityResult result = appointmentService.checkInsuranceEligibility(MRN, PAYER_ID);

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getCopayRequired()).isEqualByComparingTo("25.00");
        assertThat(result.getDeductibleRemaining()).isEqualByComparingTo("500.00");
        verify(insuranceGateway, never()).verifyEligibility(anyString(), anyString());
    }

    @Test
    void checkInsuranceEligibility_callsGatewayAndCachesOnCacheMiss() {
        when(insuranceCache.getEligibility(MRN, PAYER_ID)).thenReturn(null);
        when(insuranceGateway.verifyEligibility(MRN, PAYER_ID)).thenReturn(eligible());

        AppointmentService.EligibilityResult result = appointmentService.checkInsuranceEligibility(MRN, PAYER_ID);

        assertThat(result.isEligible()).isTrue();
        verify(insuranceCache).cacheEligibility(MRN, "XXX-XX-0001", PAYER_ID, "INS123", true,
                "Premium Health Plan", "25.00", "500.00");
    }

    @Test
    void checkInsuranceEligibility_doesNotCacheIneligibleResponses() {
        when(insuranceCache.getEligibility(MRN, PAYER_ID)).thenReturn(null);
        when(insuranceGateway.verifyEligibility(MRN, PAYER_ID)).thenReturn(
                AppointmentService.EligibilityResult.builder().eligible(false).reason("Coverage terminated").build());

        appointmentService.checkInsuranceEligibility(MRN, PAYER_ID);

        verify(insuranceCache, never()).cacheEligibility(anyString(), any(), anyString(), any(), anyBoolean(),
                any(), any(), any());
    }

    @Test
    void scheduleAppointment_returnsVerifiedAppointmentForEligiblePatient() {
        LocalDate date = LocalDate.of(2024, 4, 1);
        when(insuranceCache.getEligibility(MRN, PAYER_ID)).thenReturn(null);
        when(insuranceGateway.verifyEligibility(MRN, PAYER_ID)).thenReturn(eligible());

        Map<String, Object> appointment =
                appointmentService.scheduleAppointment(1L, MRN, PAYER_ID, date, "OFFICE_VISIT", 3L);

        assertThat(appointment)
                .containsEntry("patientId", 1L)
                .containsEntry("patientMrn", MRN)
                .containsEntry("appointmentDate", date)
                .containsEntry("appointmentType", "OFFICE_VISIT")
                .containsEntry("providerId", 3L)
                .containsEntry("eligibilityVerified", true)
                .containsEntry("status", "SCHEDULED");
        assertThat((BigDecimal) appointment.get("copayAmount")).isEqualByComparingTo("25.00");
    }

    @Test
    void scheduleAppointment_rejectsIneligiblePatient() {
        when(insuranceCache.getEligibility(MRN, PAYER_ID)).thenReturn(null);
        when(insuranceGateway.verifyEligibility(MRN, PAYER_ID)).thenReturn(
                AppointmentService.EligibilityResult.builder().eligible(false).reason("Coverage terminated").build());

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(
                1L, MRN, PAYER_ID, LocalDate.of(2024, 4, 1), "OFFICE_VISIT", 3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Coverage terminated");
    }
}
