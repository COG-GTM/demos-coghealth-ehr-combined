package com.medchart.ehr.service;

import com.medchart.ehr.config.TenantContext;
import com.medchart.ehr.domain.encounter.Encounter;
import com.medchart.ehr.domain.encounter.EncounterStatus;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.repository.EncounterRepository;
import com.medchart.ehr.repository.PatientRepository;
import com.medchart.ehr.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class EncounterService {

    private static final Logger log = LoggerFactory.getLogger(EncounterService.class);
    private static final DateTimeFormatter ENC_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    
    private final EncounterRepository encounterRepository;
    private final PatientRepository patientRepository;
    private final ProviderRepository providerRepository;
    private final TenantContext tenantContext;
    private final AtomicLong encounterSequence = new AtomicLong(100);

    public EncounterService(EncounterRepository encounterRepository,
                            PatientRepository patientRepository,
                            ProviderRepository providerRepository,
                            TenantContext tenantContext) {
        this.encounterRepository = encounterRepository;
        this.patientRepository = patientRepository;
        this.providerRepository = providerRepository;
        this.tenantContext = tenantContext;
    }

    @Transactional(readOnly = true)
    public Optional<Encounter> findById(Long id) {
        return encounterRepository.findByIdAndOrganizationId(id, tenantContext.requireOrganizationId());
    }

    @Transactional(readOnly = true)
    public Optional<Encounter> findByIdWithDetails(Long id) {
        return encounterRepository.findByIdWithDetails(id, tenantContext.requireOrganizationId());
    }

    @Transactional(readOnly = true)
    public Optional<Encounter> findByEncounterNumber(String encounterNumber) {
        return encounterRepository.findByEncounterNumberAndOrganizationId(
                encounterNumber, tenantContext.requireOrganizationId());
    }

    @Transactional(readOnly = true)
    public List<Encounter> findByPatientId(Long patientId) {
        return encounterRepository.findByOrganizationIdAndPatientId(tenantContext.requireOrganizationId(), patientId);
    }

    @Transactional(readOnly = true)
    public Page<Encounter> findByPatientId(Long patientId, Pageable pageable) {
        return encounterRepository.findByOrganizationIdAndPatientId(
                tenantContext.requireOrganizationId(), patientId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Encounter> findByProviderId(Long providerId) {
        return encounterRepository.findByOrganizationIdAndAttendingProviderId(
                tenantContext.requireOrganizationId(), providerId);
    }

    @Transactional(readOnly = true)
    public List<Encounter> getProviderSchedule(Long providerId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return encounterRepository.findTodaysSchedule(
                tenantContext.requireOrganizationId(), providerId, startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public List<Encounter> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return encounterRepository.findByDateRange(tenantContext.requireOrganizationId(),
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    @Transactional(readOnly = true)
    public List<Encounter> findByStatus(EncounterStatus status) {
        return encounterRepository.findByOrganizationIdAndStatus(tenantContext.requireOrganizationId(), status);
    }

    public Encounter create(Encounter encounter) {
        Long organizationId = tenantContext.requireOrganizationId();

        encounter.setId(null);
        encounter.setOrganizationId(organizationId);
        encounter.setPatient(requirePatientInTenant(encounter.getPatient(), organizationId));
        encounter.setAttendingProvider(requireProviderInTenant(encounter.getAttendingProvider(), organizationId));
        encounter.setEncounterNumber(generateEncounterNumber());
        encounter.setStatus(EncounterStatus.SCHEDULED);
        
        Encounter saved = encounterRepository.save(encounter);
        log.info("Created encounter {} for patient {}", saved.getEncounterNumber(), saved.getPatient().getMrn());
        return saved;
    }

    public Encounter update(Long id, Encounter encounter) {
        Long organizationId = tenantContext.requireOrganizationId();
        Encounter existing = requireInTenant(id, organizationId);

        encounter.setId(existing.getId());
        encounter.setOrganizationId(organizationId);
        encounter.setVersion(existing.getVersion());
        encounter.setEncounterNumber(existing.getEncounterNumber());
        encounter.setPatient(requirePatientInTenant(encounter.getPatient(), organizationId));
        encounter.setAttendingProvider(requireProviderInTenant(encounter.getAttendingProvider(), organizationId));

        log.info("Updating encounter {}", existing.getEncounterNumber());
        return encounterRepository.save(encounter);
    }

    public void checkIn(Long encounterId) {
        updateStatus(encounterId, EncounterStatus.CHECKED_IN, "Patient checked in for encounter {}");
    }

    public void startEncounter(Long encounterId) {
        updateStatus(encounterId, EncounterStatus.IN_PROGRESS, "Encounter {} started");
    }

    public void completeEncounter(Long encounterId, String notes) {
        Encounter encounter = requireInTenant(encounterId, tenantContext.requireOrganizationId());
        encounter.setStatus(EncounterStatus.COMPLETED);
        if (notes != null) {
            encounter.setNotes(notes);
        }
        encounterRepository.save(encounter);
        log.info("Encounter {} completed", encounter.getEncounterNumber());
    }

    public void cancelEncounter(Long encounterId) {
        updateStatus(encounterId, EncounterStatus.CANCELLED, "Encounter {} cancelled");
    }

    public void markNoShow(Long encounterId) {
        updateStatus(encounterId, EncounterStatus.NO_SHOW, "Encounter {} marked as no-show");
    }

    @Transactional(readOnly = true)
    public long getPatientEncounterCount(Long patientId) {
        return encounterRepository.countByPatientId(tenantContext.requireOrganizationId(), patientId);
    }

    private void updateStatus(Long encounterId, EncounterStatus status, String logMessage) {
        Encounter encounter = requireInTenant(encounterId, tenantContext.requireOrganizationId());
        encounter.setStatus(status);
        encounterRepository.save(encounter);
        log.info(logMessage, encounter.getEncounterNumber());
    }

    private Encounter requireInTenant(Long id, Long organizationId) {
        return encounterRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new AccessDeniedException("Encounter is not accessible in this organization"));
    }

    private Patient requirePatientInTenant(Patient patient, Long organizationId) {
        if (patient == null || patient.getId() == null) {
            throw new IllegalArgumentException("Encounter requires a patient");
        }
        return patientRepository.findByIdAndOrganizationId(patient.getId(), organizationId)
                .orElseThrow(() -> new AccessDeniedException("Patient is not accessible in this organization"));
    }

    private Provider requireProviderInTenant(Provider provider, Long organizationId) {
        if (provider == null || provider.getId() == null) {
            return null;
        }
        return providerRepository.findByIdAndOrganizationId(provider.getId(), organizationId)
                .orElseThrow(() -> new AccessDeniedException("Provider is not accessible in this organization"));
    }

    private String generateEncounterNumber() {
        String year = LocalDate.now().format(ENC_DATE_FORMAT);
        long seq = encounterSequence.incrementAndGet();
        return String.format("ENC-%s-%06d", year, seq);
    }
}
