package com.medchart.ehr.service;

import com.medchart.ehr.audit.AuditAccess;
import com.medchart.ehr.audit.AuditAction;
import com.medchart.ehr.domain.refill.RefillRequest;
import com.medchart.ehr.domain.refill.RefillRequestStatus;
import com.medchart.ehr.dto.RefillRequestDTO;
import com.medchart.ehr.repository.RefillRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RefillRequestService {

    private final RefillRequestRepository refillRequestRepository;

    @AuditAccess(action = AuditAction.READ, resourceType = "RefillRequest", description = "List pending refill requests")
    public List<RefillRequestDTO> listPendingRequests() {
        return refillRequestRepository
                .findByStatusOrderByRequestedDateDesc(RefillRequestStatus.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @AuditAccess(action = AuditAction.UPDATE, resourceType = "RefillRequest", description = "Approve refill request")
    public RefillRequestDTO approveRequest(Long id) {
        RefillRequest request = refillRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Refill request not found with id: " + id));

        if (request.getStatus() != RefillRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING refill requests can be approved");
        }

        request.setStatus(RefillRequestStatus.APPROVED);
        RefillRequest saved = refillRequestRepository.save(request);
        log.info("Approved refill request id={} for patient MRN={}", saved.getId(), saved.getPatient().getMrn());
        return toDto(saved);
    }

    @Transactional
    @AuditAccess(action = AuditAction.UPDATE, resourceType = "RefillRequest", description = "Deny refill request")
    public RefillRequestDTO denyRequest(Long id) {
        RefillRequest request = refillRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Refill request not found with id: " + id));

        if (request.getStatus() != RefillRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING refill requests can be denied");
        }

        request.setStatus(RefillRequestStatus.DENIED);
        RefillRequest saved = refillRequestRepository.save(request);
        log.info("Denied refill request id={} for patient MRN={}", saved.getId(), saved.getPatient().getMrn());
        return toDto(saved);
    }

    private RefillRequestDTO toDto(RefillRequest request) {
        return RefillRequestDTO.builder()
                .id(request.getId())
                .status(request.getStatus())
                .pharmacyName(request.getPharmacyName())
                .requestedDate(request.getRequestedDate())
                .notes(request.getNotes())
                .patient(RefillRequestDTO.PatientSummary.builder()
                        .id(request.getPatient().getId())
                        .mrn(request.getPatient().getMrn())
                        .fullName(request.getPatient().getFullName())
                        .build())
                .medication(RefillRequestDTO.MedicationSummary.builder()
                        .id(request.getMedication().getId())
                        .genericName(request.getMedication().getGenericName())
                        .brandName(request.getMedication().getBrandName())
                        .build())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
