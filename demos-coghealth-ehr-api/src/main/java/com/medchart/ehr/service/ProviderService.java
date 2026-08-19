package com.medchart.ehr.service;

import com.medchart.ehr.config.TenantContext;
import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProviderService {

    private static final Logger log = LoggerFactory.getLogger(ProviderService.class);

    private final ProviderRepository providerRepository;
    private final TenantContext tenantContext;

    public ProviderService(ProviderRepository providerRepository, TenantContext tenantContext) {
        this.providerRepository = providerRepository;
        this.tenantContext = tenantContext;
    }

    @Transactional(readOnly = true)
    public List<Provider> findAll() {
        return providerRepository.findByOrganizationId(tenantContext.requireOrganizationId());
    }

    @Transactional(readOnly = true)
    public List<Provider> findActive() {
        return providerRepository.findByOrganizationIdAndActiveTrue(tenantContext.requireOrganizationId());
    }

    @Transactional(readOnly = true)
    public Optional<Provider> findById(Long id) {
        return providerRepository.findByIdAndOrganizationId(id, tenantContext.requireOrganizationId());
    }

    @Transactional(readOnly = true)
    public Optional<Provider> findByNpi(String npi) {
        return providerRepository.findByNpiAndOrganizationId(npi, tenantContext.requireOrganizationId());
    }

    @Transactional(readOnly = true)
    public List<Provider> findByDepartment(String department) {
        return providerRepository.findByOrganizationIdAndDepartment(tenantContext.requireOrganizationId(), department);
    }

    @Transactional(readOnly = true)
    public List<Provider> findBySpecialty(String specialty) {
        return providerRepository.findByOrganizationIdAndSpecialty(tenantContext.requireOrganizationId(), specialty);
    }

    @Transactional(readOnly = true)
    public List<String> getAllDepartments() {
        return providerRepository.findAllDepartments(tenantContext.requireOrganizationId());
    }

    @Transactional(readOnly = true)
    public List<String> getAllSpecialties() {
        return providerRepository.findAllSpecialties(tenantContext.requireOrganizationId());
    }

    public Provider create(Provider provider) {
        provider.setId(null);
        provider.setOrganizationId(tenantContext.requireOrganizationId());
        log.info("Creating provider: {} {}", provider.getFirstName(), provider.getLastName());
        return providerRepository.save(provider);
    }

    public Provider update(Long id, Provider provider) {
        Provider existing = requireInTenant(id);
        provider.setId(existing.getId());
        provider.setOrganizationId(existing.getOrganizationId());
        provider.setVersion(existing.getVersion());
        log.info("Updating provider: {} {}", provider.getFirstName(), provider.getLastName());
        return providerRepository.save(provider);
    }

    public void deactivate(Long id) {
        Provider provider = requireInTenant(id);
        provider.setActive(false);
        providerRepository.save(provider);
        log.info("Deactivated provider: {}", provider.getNpi());
    }

    @Transactional(readOnly = true)
    public List<Provider> search(String lastName) {
        return providerRepository.findByOrganizationIdAndLastNameContainingIgnoreCase(
                tenantContext.requireOrganizationId(), lastName);
    }

    private Provider requireInTenant(Long id) {
        return providerRepository.findByIdAndOrganizationId(id, tenantContext.requireOrganizationId())
                .orElseThrow(() -> new AccessDeniedException("Provider is not accessible in this organization"));
    }
}
