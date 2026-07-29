package com.medchart.ehr.service;

import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.mapper.PatientMapper;
import com.medchart.ehr.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("Jane")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1980, 5, 12))
                .build();

        patientDTO = PatientDTO.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("Jane")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1980, 5, 12))
                .build();
    }

    @Test
    void getPatientByIdReturnsMappedDto() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.getPatientById(1L)).isSameAs(patientDTO);
    }

    @Test
    void getPatientByIdThrowsWhenMissing() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getPatientByMrnReturnsMappedDto() {
        when(patientRepository.findByMrn("MRN001")).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.getPatientByMrn("MRN001")).isSameAs(patientDTO);
    }

    @Test
    void getPatientByMrnThrowsWhenMissing() {
        when(patientRepository.findByMrn("MRN404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientByMrn("MRN404"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("MRN404");
    }

    @Test
    void searchPatientsMapsEachResult() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Patient> page = new PageImpl<>(Collections.singletonList(patient), pageable, 1);
        when(patientRepository.searchPatients("doe", pageable)).thenReturn(page);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        Page<PatientDTO> result = patientService.searchPatients("doe", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(patientDTO);
    }

    @Test
    void createPatientRejectsDuplicateMrn() {
        when(patientRepository.findByMrn("MRN001")).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> patientService.createPatient(patientDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MRN001");

        verify(patientRepository, never()).save(any());
    }

    @Test
    void createPatientGeneratesMrnWhenAbsent() {
        PatientDTO withoutMrn = PatientDTO.builder().firstName("New").lastName("Patient").build();
        Patient entity = Patient.builder().firstName("New").lastName("Patient").build();
        when(patientMapper.toEntity(withoutMrn)).thenReturn(entity);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(patientMapper.toDto(any(Patient.class))).thenReturn(patientDTO);

        patientService.createPatient(withoutMrn);

        ArgumentCaptor<Patient> saved = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(saved.capture());
        assertThat(saved.getValue().getMrn()).startsWith("MRN");
        verify(patientRepository, never()).findByMrn(any());
    }

    @Test
    void createPatientKeepsProvidedMrn() {
        when(patientRepository.findByMrn("MRN001")).thenReturn(Optional.empty());
        when(patientMapper.toEntity(patientDTO)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.createPatient(patientDTO)).isSameAs(patientDTO);
        assertThat(patient.getMrn()).isEqualTo("MRN001");
    }

    @Test
    void updatePatientAppliesChangesToExistingEntity() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.updatePatient(1L, patientDTO)).isSameAs(patientDTO);
        verify(patientMapper).updateEntityFromDto(patientDTO, patient);
    }

    @Test
    void updatePatientThrowsWhenMissing() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(99L, patientDTO))
                .isInstanceOf(EntityNotFoundException.class);
        verify(patientRepository, never()).save(any());
    }

    @Test
    void findBySsnIsDisabledForHipaaCompliance() {
        assertThatThrownBy(() -> patientService.findBySsn("123-45-6789"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("HIPAA");
    }
}
