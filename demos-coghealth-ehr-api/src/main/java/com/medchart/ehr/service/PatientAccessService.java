package com.medchart.ehr.service;

import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.repository.EncounterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enforces per-record authorization for patient records (PHI).
 *
 * <p>A caller may only read or modify a patient chart when they are an administrator or when the
 * user account is linked to a provider who has a documented care relationship with that patient
 * (primary provider, or attending provider on one of the patient's encounters).
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientAccessService {

    private final EncounterRepository encounterRepository;

    public void checkAccess(Patient patient) {
        if (canAccess(patient)) {
            return;
        }
        log.warn("Denied access to patient {} for user {}", patient.getId(), currentUser().getUsername());
        throw new AccessDeniedException("Not authorized to access this patient record");
    }

    public boolean canAccess(Patient patient) {
        User user = currentUser();

        if (hasRole(user, User.Role.ADMIN)) {
            return true;
        }

        Long providerId = user.getProviderId();
        return providerId != null && hasCareRelationship(patient, providerId);
    }

    /** Provider record linked to the authenticated user, or {@code null} for non-provider accounts. */
    public Long currentProviderId() {
        return currentUser().getProviderId();
    }

    private boolean hasCareRelationship(Patient patient, Long providerId) {
        if (providerId.equals(patient.getPrimaryProviderId())) {
            return true;
        }
        return encounterRepository.existsByPatientIdAndAttendingProviderId(patient.getId(), providerId);
    }

    private boolean hasRole(User user, User.Role role) {
        return user.getRoles() != null && user.getRoles().contains(role);
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("Authentication required to access patient records");
        }
        return (User) authentication.getPrincipal();
    }
}
