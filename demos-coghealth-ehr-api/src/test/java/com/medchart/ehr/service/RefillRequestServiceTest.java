package com.medchart.ehr.service;

import com.medchart.ehr.domain.medication.Medication;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.domain.refill.RefillRequest;
import com.medchart.ehr.domain.refill.RefillRequestStatus;
import com.medchart.ehr.dto.RefillRequestDTO;
import com.medchart.ehr.repository.RefillRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefillRequestServiceTest {

    @Mock
    private RefillRequestRepository refillRequestRepository;

    @InjectMocks
    private RefillRequestService refillRequestService;

    private Patient patient;
    private Medication medication;
    private RefillRequest pendingRequest;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setMrn("MRN-2019-00001");
        patient.setFirstName("John");
        patient.setLastName("Smith");

        medication = new Medication();
        medication.setId(2L);
        medication.setGenericName("Lisinopril");
        medication.setBrandName("Prinivil");

        pendingRequest = RefillRequest.builder()
                .id(10L)
                .patient(patient)
                .medication(medication)
                .pharmacyName("CVS Pharmacy")
                .status(RefillRequestStatus.PENDING)
                .requestedDate(LocalDate.of(2024, 3, 15))
                .build();
    }

    @Test
    void listPendingRequests_returnsPendingRequestsMappedToDto() {
        when(refillRequestRepository.findByStatusOrderByRequestedDateDesc(RefillRequestStatus.PENDING))
                .thenReturn(Collections.singletonList(pendingRequest));

        List<RefillRequestDTO> result = refillRequestService.listPendingRequests();

        assertThat(result).hasSize(1);
        RefillRequestDTO dto = result.get(0);
        assertThat(dto.getStatus()).isEqualTo(RefillRequestStatus.PENDING);
        assertThat(dto.getPharmacyName()).isEqualTo("CVS Pharmacy");
        assertThat(dto.getPatient().getFullName()).isEqualTo("John Smith");
        assertThat(dto.getMedication().getGenericName()).isEqualTo("Lisinopril");
    }

    @Test
    void approveRequest_approvesPendingRequest() {
        when(refillRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(refillRequestRepository.save(any(RefillRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        RefillRequestDTO result = refillRequestService.approveRequest(10L);

        assertThat(result.getStatus()).isEqualTo(RefillRequestStatus.APPROVED);
        verify(refillRequestRepository).save(pendingRequest);
    }

    @Test
    void approveRequest_throwsWhenNotPending() {
        pendingRequest.setStatus(RefillRequestStatus.DENIED);
        when(refillRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));

        assertThatThrownBy(() -> refillRequestService.approveRequest(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING");

        verify(refillRequestRepository, never()).save(any());
    }

    @Test
    void approveRequest_throwsWhenMissing() {
        when(refillRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refillRequestService.approveRequest(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void denyRequest_deniesPendingRequest() {
        when(refillRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(refillRequestRepository.save(any(RefillRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        RefillRequestDTO result = refillRequestService.denyRequest(10L);

        assertThat(result.getStatus()).isEqualTo(RefillRequestStatus.DENIED);
        verify(refillRequestRepository).save(pendingRequest);
    }

    @Test
    void denyRequest_throwsWhenAlreadyApproved() {
        pendingRequest.setStatus(RefillRequestStatus.APPROVED);
        when(refillRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));

        assertThatThrownBy(() -> refillRequestService.denyRequest(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING");
    }
}
