package com.medchart.ehr.service;

import com.medchart.ehr.audit.AuditAccess;
import com.medchart.ehr.audit.AuditAction;
import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProviderService {

    private static final Logger log = LoggerFactory.getLogger(ProviderService.class);

    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Transactional(readOnly = true)
    public List<Provider> findAll() {
        return providerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Provider> findActive() {
        return providerRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Provider> findById(Long id) {
        return providerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Provider> findByNpi(String npi) {
        return providerRepository.findByNpi(npi);
    }

    @Transactional(readOnly = true)
    public List<Provider> findByDepartment(String department) {
        return providerRepository.findByDepartment(department);
    }

    @Transactional(readOnly = true)
    public List<Provider> findBySpecialty(String specialty) {
        return providerRepository.findBySpecialty(specialty);
    }

    @Transactional(readOnly = true)
    public List<String> getAllDepartments() {
        return providerRepository.findAllDepartments();
    }

    @Transactional(readOnly = true)
    public List<String> getAllSpecialties() {
        return providerRepository.findAllSpecialties();
    }

    @AuditAccess(action = AuditAction.CREATE, resourceType = "Provider", description = "Create provider record")
    public Provider create(Provider provider) {
        log.info("Creating provider: {} {}", provider.getFirstName(), provider.getLastName());
        return providerRepository.save(provider);
    }

    @AuditAccess(action = AuditAction.UPDATE, resourceType = "Provider", description = "Update provider record")
    public Provider update(Provider provider) {
        log.info("Updating provider: {} {}", provider.getFirstName(), provider.getLastName());
        return providerRepository.save(provider);
    }

    @AuditAccess(action = AuditAction.UPDATE, resourceType = "Provider", description = "Deactivate provider record")
    public boolean deactivate(Long id) {
        return providerRepository.findById(id).map(provider -> {
            provider.setActive(false);
            providerRepository.save(provider);
            log.info("Deactivated provider: {}", provider.getNpi());
            return true;
        }).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Provider> search(String lastName) {
        return providerRepository.findByLastNameContainingIgnoreCase(lastName);
    }
}
