package com.medchart.ehr.service;

import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.repository.EncounterRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class PatientAccessServiceTest {

    private EncounterRepository encounterRepository;
    private PatientAccessService patientAccessService;

    @BeforeEach
    void setUp() {
        encounterRepository = Mockito.mock(EncounterRepository.class);
        patientAccessService = new PatientAccessService(encounterRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deniesAnonymousCallers() {
        Patient patient = patient(1L, 7L);

        assertThrows(AccessDeniedException.class, () -> patientAccessService.checkAccess(patient));
    }

    @Test
    void deniesProviderWithoutCareRelationship() {
        authenticate(user(9L, User.Role.PROVIDER));
        when(encounterRepository.existsByPatientIdAndAttendingProviderId(anyLong(), anyLong())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> patientAccessService.checkAccess(patient(1L, 7L)));
    }

    @Test
    void deniesUserWithoutLinkedProviderRecord() {
        authenticate(user(null, User.Role.STAFF));

        assertThrows(AccessDeniedException.class, () -> patientAccessService.checkAccess(patient(1L, 7L)));
    }

    @Test
    void allowsPrimaryProvider() {
        authenticate(user(7L, User.Role.PROVIDER));

        assertDoesNotThrow(() -> patientAccessService.checkAccess(patient(1L, 7L)));
    }

    @Test
    void allowsAttendingProviderOfAnEncounter() {
        authenticate(user(9L, User.Role.PROVIDER));
        when(encounterRepository.existsByPatientIdAndAttendingProviderId(1L, 9L)).thenReturn(true);

        assertDoesNotThrow(() -> patientAccessService.checkAccess(patient(1L, 7L)));
    }

    @Test
    void allowsAdministrators() {
        authenticate(user(null, User.Role.ADMIN));

        assertDoesNotThrow(() -> patientAccessService.checkAccess(patient(1L, 7L)));
    }

    private Patient patient(Long id, Long primaryProviderId) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setPrimaryProviderId(primaryProviderId);
        return patient;
    }

    private User user(Long providerId, User.Role role) {
        User user = new User();
        user.setUsername("test.user");
        user.setProviderId(providerId);
        user.setRoles(Set.of(role));
        return user;
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
    }
}
