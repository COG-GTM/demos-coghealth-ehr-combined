package com.medchart.ehr.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    static class AuditedTarget {
        @AuditAccess(action = AuditAction.READ, resourceType = "Patient", description = "View patient record")
        public String read(Long patientId) {
            return "chart";
        }
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void stubJoinPoint(Object... args) throws NoSuchMethodException {
        Method method = AuditedTarget.class.getMethod("read", Long.class);
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
    @DisplayName("a successful call records the annotation metadata and the patient id argument")
    void successfulCallIsAudited() throws Throwable {
        stubJoinPoint(42L);
        when(joinPoint.proceed()).thenReturn("chart");

        assertThat(auditAspect.auditAccess(joinPoint)).isEqualTo("chart");

        AuditEvent event = capturedEvent();
        assertThat(event.getAction()).isEqualTo(AuditAction.READ);
        assertThat(event.getResourceType()).isEqualTo("Patient");
        assertThat(event.getDescription()).isEqualTo("View patient record");
        assertThat(event.getPatientId()).isEqualTo(42L);
        assertThat(event.getSuccess()).isTrue();
        assertThat(event.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("a failed call is audited with the error message and the exception is rethrown")
    void failedCallIsAuditedAndRethrown() throws Throwable {
        stubJoinPoint(42L);
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> auditAspect.auditAccess(joinPoint))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        AuditEvent event = capturedEvent();
        assertThat(event.getSuccess()).isFalse();
        assertThat(event.getErrorMessage()).isEqualTo("boom");
    }

    @Test
    @DisplayName("no patient id is recorded when the call has no Long argument")
    void patientIdIsNullWithoutLongArgument() throws Throwable {
        stubJoinPoint("MRN001");
        when(joinPoint.proceed()).thenReturn("chart");

        auditAspect.auditAccess(joinPoint);

        assertThat(capturedEvent().getPatientId()).isNull();
    }

    @Test
    @DisplayName("the client IP is taken from X-Forwarded-For when the proxy sets it")
    void clientIpPrefersForwardedForHeader() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.1");
        request.addHeader("User-Agent", "CogHealth/1.0");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        stubJoinPoint(42L);
        when(joinPoint.proceed()).thenReturn("chart");

        auditAspect.auditAccess(joinPoint);

        AuditEvent event = capturedEvent();
        assertThat(event.getIpAddress()).isEqualTo("203.0.113.7");
        assertThat(event.getUserAgent()).isEqualTo("CogHealth/1.0");
    }

    @Test
    @DisplayName("the remote address is used when no proxy header is present")
    void clientIpFallsBackToRemoteAddress() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        stubJoinPoint(42L);
        when(joinPoint.proceed()).thenReturn("chart");

        auditAspect.auditAccess(joinPoint);

        assertThat(capturedEvent().getIpAddress()).isEqualTo("10.0.0.1");
    }

    @Test
    @DisplayName("auditing works outside of a web request")
    void ipAndUserAgentAreNullWithoutRequestContext() throws Throwable {
        RequestContextHolder.resetRequestAttributes();
        stubJoinPoint(42L);
        when(joinPoint.proceed()).thenReturn("chart");

        auditAspect.auditAccess(joinPoint);

        AuditEvent event = capturedEvent();
        assertThat(event.getIpAddress()).isNull();
        assertThat(event.getUserAgent()).isNull();
    }
}
