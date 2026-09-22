package com.medchart.ehr.service;

import com.medchart.ehr.audit.AuditAction;
import com.medchart.ehr.audit.PatientAccessLogger;
import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.repository.EncounterRepository;
import com.medchart.ehr.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Authorization for PHI export/report endpoints.
 *
 * Export documents are served by patient id supplied by the caller, so every export
 * must be tied to the authenticated user and to a treatment relationship with that
 * patient. Denied attempts are audited as failed PHI access.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportAuthorizationService {

    private final ProviderRepository providerRepository;
    private final EncounterRepository encounterRepository;
    private final PatientAccessLogger patientAccessLogger;

    /**
     * Authorizes the current user to export a single patient's documents.
     * Administrators are allowed; providers only for patients they treat.
     */
    public User authorizePatientExport(Long patientId, String resourceType) {
        User user = requireAuthenticatedUser();

        if (patientId == null) {
            throw new IllegalArgumentException("Patient id is required");
        }

        if (hasRole(user, User.Role.ADMIN) || isTreatingProvider(user, patientId)) {
            patientAccessLogger.logAccess(user.getId(), roleNames(user), patientId, null,
                    AuditAction.EXPORT, resourceType, "Patient document export", null, null);
            return user;
        }

        patientAccessLogger.logFailedAccess(user.getId(), roleNames(user), patientId,
                AuditAction.EXPORT, resourceType, "No treatment relationship with patient", null);
        throw new AccessDeniedException("Not authorized to export data for this patient");
    }

    /**
     * Authorizes the current user for exports/reports that span multiple patients.
     */
    public User authorizeBulkExport(String resourceType) {
        User user = requireAuthenticatedUser();

        if (hasRole(user, User.Role.ADMIN)) {
            log.info("Bulk export {} authorized for user {}", resourceType, user.getId());
            return user;
        }

        patientAccessLogger.logFailedAccess(user.getId(), roleNames(user), null,
                AuditAction.EXPORT, resourceType, "Bulk PHI export requires ADMIN role", null);
        throw new AccessDeniedException("Not authorized to export data for multiple patients");
    }

    private User requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("Authentication is required to export patient data");
        }
        return (User) authentication.getPrincipal();
    }

    private boolean isTreatingProvider(User user, Long patientId) {
        if (!hasRole(user, User.Role.PROVIDER)) {
            return false;
        }
        Optional<Provider> provider = providerRepository.findByEmailIgnoreCase(user.getEmail());
        return provider
                .map(p -> encounterRepository.existsByPatientIdAndAttendingProviderId(patientId, p.getId()))
                .orElse(false);
    }

    private boolean hasRole(User user, User.Role role) {
        return user.getRoles() != null && user.getRoles().contains(role);
    }

    private String roleNames(User user) {
        return user.getRoles() == null ? "" : user.getRoles().toString();
    }
}
