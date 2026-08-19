package com.medchart.ehr.service;

import com.medchart.ehr.audit.PatientAccessLogger;
import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.repository.EncounterRepository;
import com.medchart.ehr.repository.ProviderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportAuthorizationServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private PatientAccessLogger patientAccessLogger;

    @InjectMocks
    private ExportAuthorizationService service;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
    }

    private User user(Long id, String email, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(email);
        user.setEmail(email);
        user.setRoles(Set.of(role));
        return user;
    }

    @Test
    void anonymousCallerCannotExportPatientDocuments() {
        assertThrows(AccessDeniedException.class,
                () -> service.authorizePatientExport(1L, "EncounterHistoryExport"));
    }

    @Test
    void providerCannotExportAnotherPatientsDocuments() {
        User provider = user(7L, "sarah.chen@medchart.local", User.Role.PROVIDER);
        authenticate(provider);
        when(providerRepository.findByEmailIgnoreCase(provider.getEmail()))
                .thenReturn(Optional.of(providerEntity(42L)));
        when(encounterRepository.existsByPatientIdAndAttendingProviderId(99L, 42L)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.authorizePatientExport(99L, "EncounterHistoryExport"));
        verify(patientAccessLogger).logFailedAccess(eq(7L), anyString(), eq(99L), any(), anyString(),
                anyString(), isNull());
    }

    @Test
    void providerCanExportDocumentsForTreatedPatient() {
        User provider = user(7L, "sarah.chen@medchart.local", User.Role.PROVIDER);
        authenticate(provider);
        when(providerRepository.findByEmailIgnoreCase(provider.getEmail()))
                .thenReturn(Optional.of(providerEntity(42L)));
        when(encounterRepository.existsByPatientIdAndAttendingProviderId(5L, 42L)).thenReturn(true);

        assertEquals(provider, service.authorizePatientExport(5L, "EncounterHistoryExport"));
        verify(patientAccessLogger).logAccess(eq(7L), anyString(), eq(5L), isNull(), any(), anyString(),
                anyString(), isNull(), isNull());
    }

    @Test
    void staffWithoutTreatmentRelationshipIsDenied() {
        authenticate(user(9L, "front.desk@medchart.local", User.Role.STAFF));

        assertThrows(AccessDeniedException.class,
                () -> service.authorizePatientExport(5L, "EncounterHistoryExport"));
    }

    @Test
    void adminCanExportAnyPatientAndBulkData() {
        User admin = user(1L, "admin@medchart.com", User.Role.ADMIN);
        authenticate(admin);

        assertEquals(admin, service.authorizePatientExport(123L, "EncounterHistoryExport"));
        assertEquals(admin, service.authorizeBulkExport("PatientRosterReport"));
    }

    @Test
    void nonAdminCannotRunBulkExports() {
        authenticate(user(7L, "sarah.chen@medchart.local", User.Role.PROVIDER));

        assertThrows(AccessDeniedException.class, () -> service.authorizeBulkExport("PatientRosterReport"));
        verify(patientAccessLogger).logFailedAccess(eq(7L), anyString(), isNull(), any(), anyString(),
                anyString(), isNull());
    }

    private Provider providerEntity(Long id) {
        Provider provider = new Provider();
        provider.setId(id);
        return provider;
    }
}
