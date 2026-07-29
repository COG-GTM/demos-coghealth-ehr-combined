package com.medchart.ehr.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @InjectMocks
    private AuditAspect auditAspect;

    /** Stand-in for an audited service method. */
    static class AuditedService {
        @AuditAccess(action = AuditAction.READ, resourceType = "Patient", description = "View patient record")
        PatientRecord viewPatient(Long patientId) {
            return new PatientRecord();
        }

        @AuditAccess(action = AuditAction.SEARCH, resourceType = "Patient")
        PatientRecord searchPatients(String term) {
            return new PatientRecord();
        }
    }

    static class PatientRecord {
    }

    private void stubJoinPoint(String methodName, Class<?> paramType, Object... args) throws Exception {
        Method method = AuditedService.class.getDeclaredMethod(methodName, paramType);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
    }

    private AuditEvent capturedEvent() {
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditService).saveAuditEventAsync(captor.capture());
        return captor.getValue();
    }

    @Test
    void successfulCallIsAuditedWithAnnotationMetadata() throws Throwable {
        stubJoinPoint("viewPatient", Long.class, 42L);
        PatientRecord record = new PatientRecord();
        when(joinPoint.proceed()).thenReturn(record);

        Object result = auditAspect.auditAccess(joinPoint);

        assertThat(result).isSameAs(record);
        AuditEvent event = capturedEvent();
        assertThat(event.getAction()).isEqualTo(AuditAction.READ);
        assertThat(event.getResourceType()).isEqualTo("Patient");
        assertThat(event.getDescription()).isEqualTo("View patient record");
        assertThat(event.getPatientId()).isEqualTo(42L);
        assertThat(event.getSuccess()).isTrue();
        assertThat(event.getUserId()).isNotBlank();
    }

    @Test
    void failedCallIsAuditedAndTheExceptionIsRethrown() throws Throwable {
        stubJoinPoint("viewPatient", Long.class, 42L);
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("database down"));

        assertThatThrownBy(() -> auditAspect.auditAccess(joinPoint))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("database down");

        AuditEvent event = capturedEvent();
        assertThat(event.getSuccess()).isFalse();
        assertThat(event.getErrorMessage()).isEqualTo("database down");
    }

    @Test
    void patientIdIsNullWhenNoIdentifierArgumentIsPresent() throws Throwable {
        stubJoinPoint("searchPatients", String.class, "doe");
        when(joinPoint.proceed()).thenReturn(new PatientRecord());

        auditAspect.auditAccess(joinPoint);

        AuditEvent event = capturedEvent();
        assertThat(event.getPatientId()).isNull();
        assertThat(event.getAction()).isEqualTo(AuditAction.SEARCH);
        assertThat(event.getDescription()).isEmpty();
    }
}
