package com.medchart.ehr.config;

import com.medchart.ehr.domain.auth.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the organization (tenant) of the authenticated caller. Every data
 * access path must scope its queries to this organization so that a provider
 * in one tenant cannot read or modify another tenant's records.
 */
@Component
public class TenantContext {

    public User requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new AccessDeniedException("Authenticated principal is not bound to an organization");
        }
        return (User) principal;
    }

    public Long requireOrganizationId() {
        Long organizationId = requireUser().getOrganizationId();
        if (organizationId == null) {
            throw new AccessDeniedException("Authenticated user is not bound to an organization");
        }
        return organizationId;
    }
}
