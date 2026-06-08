package com.medchart.ehr.service;

import com.medchart.ehr.audit.AuditAccess;
import com.medchart.ehr.audit.AuditAction;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.mapper.PatientMapper;
import com.medchart.ehr.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final EmbeddingService embeddingService;

    @AuditAccess(action = AuditAction.READ, resourceType = "Patient", description = "View patient record")
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));
        return patientMapper.toDto(patient);
    }

    @AuditAccess(action = AuditAction.READ, resourceType = "Patient", description = "View patient by MRN")
    public PatientDTO getPatientByMrn(String mrn) {
        Patient patient = patientRepository.findByMrn(mrn)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with MRN: " + mrn));
        return patientMapper.toDto(patient);
    }

    @AuditAccess(action = AuditAction.SEARCH, resourceType = "Patient", description = "Search patients")
    public Page<PatientDTO> searchPatients(String searchTerm, Pageable pageable) {
        return patientRepository.searchPatients(searchTerm, pageable)
                .map(patientMapper::toDto);
    }

    @AuditAccess(action = AuditAction.SEARCH, resourceType = "Patient", description = "Vector search patients")
    public List<PatientDTO> vectorSearchPatients(String searchTerm, int limit) {
        float[] embedding = embeddingService.getEmbedding(searchTerm);
        String vectorString = embeddingService.toVectorString(embedding);
        return patientRepository.vectorSearch(vectorString, limit)
                .stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePatientEmbedding(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + patientId));
        String text = embeddingService.toSearchableText(patient);
        float[] embedding = embeddingService.getEmbedding(text);
        String vectorString = embeddingService.toVectorString(embedding);
        patientRepository.updateEmbedding(patientId, vectorString);
    }

    @Transactional
    public void backfillAllEmbeddings() {
        List<Patient> allPatients = patientRepository.findAll();
        for (Patient patient : allPatients) {
            try {
                updatePatientEmbedding(patient.getId());
                log.info("Updated embedding for patient MRN: {}", patient.getMrn());
            } catch (Exception e) {
                log.warn("Failed to generate embedding for patient {}: {}", patient.getMrn(), e.getMessage());
            }
        }
    }

    @Transactional
    @AuditAccess(action = AuditAction.CREATE, resourceType = "Patient", description = "Create patient record")
    public PatientDTO createPatient(PatientDTO patientDTO) {
        if (patientDTO.getMrn() != null) {
            Optional<Patient> existing = patientRepository.findByMrn(patientDTO.getMrn());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Patient with MRN " + patientDTO.getMrn() + " already exists");
            }
        }

        Patient patient = patientMapper.toEntity(patientDTO);
        if (patient.getMrn() == null) {
            patient.setMrn(generateMrn());
        }

        Patient saved = patientRepository.save(patient);
        log.info("Created patient with MRN: {}", saved.getMrn());
        try {
            updatePatientEmbedding(saved.getId());
        } catch (Exception e) {
            log.warn("Failed to generate embedding for patient {}: {}", saved.getMrn(), e.getMessage());
        }
        return patientMapper.toDto(saved);
    }

    @Transactional
    @AuditAccess(action = AuditAction.UPDATE, resourceType = "Patient", description = "Update patient record")
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));

        patientMapper.updateEntityFromDto(patientDTO, existing);
        Patient saved = patientRepository.save(existing);
        log.info("Updated patient with MRN: {}", saved.getMrn());
        try {
            updatePatientEmbedding(saved.getId());
        } catch (Exception e) {
            log.warn("Failed to update embedding for patient {}: {}", saved.getMrn(), e.getMessage());
        }
        return patientMapper.toDto(saved);
    }

    /**
     * @deprecated SSN-based patient lookup is deprecated for HIPAA compliance.
     * Use getPatientByMrn() or searchPatients() instead.
     */
    @Deprecated
    public Optional<PatientDTO> findBySsn(String ssn) {
        throw new UnsupportedOperationException(
            "SSN-based patient lookup is disabled for HIPAA compliance. " +
            "Use MRN or name-based search instead.");
    }

    private String generateMrn() {
        return "MRN" + System.currentTimeMillis();
    }
}
