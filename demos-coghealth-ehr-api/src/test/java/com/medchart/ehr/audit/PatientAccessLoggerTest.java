package com.medchart.ehr.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PatientAccessLoggerTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @InjectMocks
    private PatientAccessLogger patientAccessLogger;

    private AuditEvent savedEvent() {
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void logAccessPersistsWhoWhatWhenAndWhere() {
        patientAccessLogger.logAccess(7L, "Physician", 42L, "MRN001", AuditAction.READ,
                "Patient", "Chart review", "10.0.0.1", "session-1");

        AuditEvent event = savedEvent();
        assertThat(event.getUserId()).isEqualTo("7");
        assertThat(event.getUserName()).isEqualTo("Physician");
        assertThat(event.getPatientId()).isEqualTo(42L);
        assertThat(event.getPatientMrn()).isEqualTo("MRN001");
        assertThat(event.getAction()).isEqualTo(AuditAction.READ);
        assertThat(event.getResourceType()).isEqualTo("Patient");
        assertThat(event.getDescription()).isEqualTo("Chart review");
        assertThat(event.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(event.getSessionId()).isEqualTo("session-1");
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getSuccess()).isTrue();
    }

    @Test
    void logFailedAccessMarksTheEventUnsuccessful() {
        patientAccessLogger.logFailedAccess(7L, "Nurse", 42L, AuditAction.ACCESS_DENIED,
                "Patient", "Not on care team", "10.0.0.1");

        AuditEvent event = savedEvent();
        assertThat(event.getSuccess()).isFalse();
        assertThat(event.getAction()).isEqualTo(AuditAction.ACCESS_DENIED);
        assertThat(event.getDescription()).isEqualTo("Not on care team");
    }

    @Test
    void logBulkAccessRecordsTheExportedRecordCount() {
        patientAccessLogger.logBulkAccess(7L, "Analyst", AuditAction.EXPORT,
                "Encounter", 250, "Quarterly reporting", "10.0.0.1");

        AuditEvent event = savedEvent();
        assertThat(event.getAction()).isEqualTo(AuditAction.EXPORT);
        assertThat(event.getDescription()).isEqualTo("Quarterly reporting [BULK: 250 records]");
        assertThat(event.getPatientId()).isNull();
    }

    @Test
    void generateAccessReportEchoesTheRequestedWindow() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 3, 31, 23, 59);

        Map<String, Object> report = patientAccessLogger.generateAccessReport(42L, start, end);

        assertThat(report)
                .containsEntry("patientId", 42L)
                .containsEntry("startDate", start)
                .containsEntry("endDate", end)
                .containsKeys("generatedAt", "totalAccesses", "uniqueUsers", "accessByType");
    }
}
